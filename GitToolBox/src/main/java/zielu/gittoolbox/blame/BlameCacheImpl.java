package zielu.gittoolbox.blame;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Timer;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import git4idea.repo.GitRepository;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.cache.VirtualFileRepoCache;
import zielu.gittoolbox.metrics.ProjectMetrics;
import zielu.gittoolbox.revision.RevisionInfo;
import zielu.gittoolbox.util.Cached;
import zielu.gittoolbox.util.CachedFactory;
import zielu.gittoolbox.util.ExecutableTask;

class BlameCacheImpl implements BlameCache, Disposable {
  private static final Logger LOG = Logger.getInstance(BlameCacheImpl.class);
  private final BlameCacheGateway gateway;
  private final VirtualFileRepoCache fileRepoCache;
  private final BlameLoader blameLoader;
  private final Map<VirtualFile, Cached<BlameAnnotation>> annotations = new ConcurrentHashMap<>();
  private final Set<VirtualFile> queued = ContainerUtil.newConcurrentSet();
  private final Timer getTimer;
  private final Counter discardedSubmitCounter;
  private final Counter invalidatedCounter;
  private final LoaderTimers loaderTimers;
  private final BlameCacheExecutor executor;

  BlameCacheImpl(@NotNull BlameCacheGateway gateway, @NotNull VirtualFileRepoCache fileRepoCache,
                 @NotNull BlameLoader blameLoader, @NotNull BlameCacheExecutor executor,
                 @NotNull ProjectMetrics metrics) {
    this.gateway = gateway;
    this.fileRepoCache = fileRepoCache;
    this.blameLoader = blameLoader;
    this.executor = executor;
    metrics.gauge("blame-cache.size", annotations::size);
    metrics.gauge("blame-cache.queue-count", queued::size);
    discardedSubmitCounter = metrics.counter("blame-cache.discarded-count");
    getTimer = metrics.timer("blame-cache.get");
    invalidatedCounter = metrics.counter("blame-cache.invalidated-count");
    Timer loadTimer = metrics.timer("blame-cache.load");
    Timer queueWaitTimer = metrics.timer("blame-cache.queue-wait");
    loaderTimers = new LoaderTimers(loadTimer, queueWaitTimer);
    this.gateway.disposeWithProject(this);
  }

  @Override
  public void dispose() {
    annotations.clear();
    queued.clear();
  }

  @NotNull
  @Override
  public BlameAnnotation getAnnotation(@NotNull VirtualFile file) {
    return getTimer.timeSupplier(() -> getAnnotationInternal(file));
  }

  private BlameAnnotation getAnnotationInternal(@NotNull VirtualFile file) {
    Cached<BlameAnnotation> cached = annotations.compute(file, this::computeCachedAnnotation);
    if (cached.isLoading() && cached.isEmpty()) {
      tryTaskSubmission(file);
      return BlameAnnotation.EMPTY;
    }
    return cached.value();
  }

  private Cached<BlameAnnotation> computeCachedAnnotation(@NotNull VirtualFile file, Cached<BlameAnnotation> cached) {
    if (cached == null) {
      return CachedFactory.loading();
    } else {
      if (cached.isLoading()) {
        if (cached.isEmpty()) {
          return CachedFactory.loading(BlameAnnotation.EMPTY);
        } else {
          return cached;
        }
      } else {
        return handleLoadedAnnotation(file, cached);
      }
    }
  }

  @NotNull
  private Cached<BlameAnnotation> handleLoadedAnnotation(@NotNull VirtualFile file,
                                                         @NotNull Cached<BlameAnnotation> cached) {
    BlameAnnotation annotation = cached.value();
    if (isChanged(file, annotation)) {
      LOG.debug("Annotation changed for ", file);
      return CachedFactory.loading();
    } else {
      LOG.debug("Annotation not changed for ", file);
      return cached;
    }
  }

  private void tryTaskSubmission(@NotNull VirtualFile file) {
    if (queued.add(file)) {
      LOG.debug("Add annotation task for ", file);
      AnnotationLoader loaderTask = new AnnotationLoader(
          file, blameLoader, loaderTimers, this::annotationLoaded);
      executor.execute(loaderTask);
    } else {
      LOG.debug("Discard annotation task for ", file);
      discardedSubmitCounter.inc();
    }
  }

  private boolean isChanged(@NotNull VirtualFile file, @NotNull BlameAnnotation annotation) {
    VcsRevisionNumber currentRevision = currentRepoRevision(file);
    return annotation.isChanged(currentRevision);
  }

  private void annotationLoaded(@NotNull VirtualFile file, @NotNull BlameAnnotation annotation) {
    if (queued.remove(file)) {
      annotations.put(file, CachedFactory.loaded(annotation));
      gateway.fireBlameUpdated(file, annotation);
    }
  }

  @NotNull
  private VcsRevisionNumber currentRepoRevision(@NotNull VirtualFile file) {
    GitRepository repo = fileRepoCache.getRepoForFile(file);
    if (repo != null) {
      try {
        VcsRevisionNumber parsedRevision = blameLoader.getCurrentRevision(repo);
        if (parsedRevision != null) {
          return parsedRevision;
        }
      } catch (VcsException e) {
        LOG.warn("Could not get current repoRevision for " + file, e);
      }
    }
    return VcsRevisionNumber.NULL;
  }

  @Override
  public void invalidate(@NotNull VirtualFile file) {
    if (annotations.remove(file) != null) {
      invalidatedCounter.inc();
      gateway.fireBlameInvalidated(file);
    }
  }

  @Override
  public void refreshForRoot(@NotNull VirtualFile root) {
    LOG.debug("Refresh for root: ", root);
    annotations.keySet().stream()
        .filter(file -> VfsUtilCore.isAncestor(root, file, false))
        .peek(file -> LOG.debug("Invalidate ", file, " under root ", root))
        .forEach(this::invalidate);
  }

  @Override
  public void revisionUpdated(@NotNull RevisionInfo revisionInfo) {
    annotations.forEach((file, cached) -> {
      if (!cached.isLoading()) {
        BlameAnnotation blame = cached.value();
        if (blame.updateRevision(revisionInfo)) {
          gateway.fireBlameUpdated(file, blame);
        }
      }
    });
  }

  private static class LoaderTimers {
    final Timer load;
    final Timer queueWait;

    private LoaderTimers(Timer load, Timer queueWait) {
      this.load = load;
      this.queueWait = queueWait;
    }
  }

  private static class AnnotationLoader implements ExecutableTask {
    private final VirtualFile file;
    private final BlameLoader loader;
    private final BiConsumer<VirtualFile, BlameAnnotation> loaded;
    private final LoaderTimers timers;
    private final long createdAt = System.currentTimeMillis();

    private AnnotationLoader(@NotNull VirtualFile file, @NotNull BlameLoader loader,
                             @NotNull LoaderTimers timers,
                             @NotNull BiConsumer<VirtualFile, BlameAnnotation> loaded) {
      this.file = file;
      this.loader = loader;
      this.timers = timers;
      this.loaded = loaded;
    }

    @Override
    public void run() {
      timers.queueWait.update(System.currentTimeMillis() - createdAt, TimeUnit.MILLISECONDS);
      BlameAnnotation annotation = timers.load.timeSupplier(this::load);
      LOG.info("Annotated " + file + ": " + annotation);
      loaded.accept(file, annotation);
    }

    @Override
    public String getTitle() {
      return "Loading annotation";
    }

    @NotNull
    private BlameAnnotation load() {
      LOG.debug("Annotate ", file);
      try {
        return loader.annotate(file);
      } catch (VcsException e) {
        LOG.warn("Failed to annotate " + file, e);
        return BlameAnnotation.EMPTY;
      }
    }
  }
}
