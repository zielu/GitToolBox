package zielu.gittoolbox.blame;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Timer;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import git4idea.repo.GitRepository;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.cache.VirtualFileRepoCache;
import zielu.gittoolbox.metrics.ProjectMetrics;
import zielu.gittoolbox.util.Cached;
import zielu.gittoolbox.util.CachedFactory;
import zielu.gittoolbox.util.ExecutableTask;

class BlameCacheImpl implements BlameCache, Disposable {
  private static final Blamed EMPTY_BLAMED = new Blamed(BlameAnnotation.EMPTY);
  private static final Logger LOG = Logger.getInstance(BlameCacheImpl.class);
  private final BlameCacheGateway gateway;
  private final VirtualFileRepoCache fileRepoCache;
  private final BlameLoader blameLoader;
  private final Cache<VirtualFile, Cached<Blamed>> annotations = CacheBuilder.newBuilder()
      .maximumSize(75)
      .expireAfterAccess(Duration.ofMinutes(45))
      .build();
  private final Set<VirtualFile> queued = ContainerUtil.newConcurrentSet();
  private final Timer getTimer;
  private final Counter discardedSubmitCounter;
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
    Timer loadTimer = metrics.timer("blame-cache.load");
    Timer queueWaitTimer = metrics.timer("blame-cache.queue-wait");
    loaderTimers = new LoaderTimers(loadTimer, queueWaitTimer);
    this.gateway.disposeWithProject(this);
  }

  @Override
  public void dispose() {
    annotations.invalidateAll();
    queued.clear();
  }

  @NotNull
  @Override
  public BlameAnnotation getAnnotation(@NotNull VirtualFile file) {
    return getTimer.timeSupplier(() -> getAnnotationInternal(file));
  }

  private BlameAnnotation getAnnotationInternal(@NotNull VirtualFile file) {
    Cached<Blamed> cached = handleDirty(file);
    if (cached == null) {
      cached = getCached(file);
    }
    if (cached.isLoading() && cached.isEmpty()) {
      return triggerReload(file);
    }
    return cached.value().annotation;
  }

  private Cached<Blamed> handleDirty(@NotNull VirtualFile file) {
    Cached<Blamed> cached = annotations.getIfPresent(file);
    if (cached != null && cached.isLoaded()) {
      Blamed blamed = cached.value();
      if (blamed.checkDirty() && isChanged(file, blamed.annotation)) {
        annotations.invalidate(file);
        cached = null;
      }
    }
    return cached;
  }

  private Cached<Blamed> getCached(@NotNull VirtualFile file) {
    try {
      return annotations.get(file, CachedFactory::loading);
    } catch (ExecutionException e) {
      LOG.warn("Failed to get cached " + file, e);
      return CachedFactory.loaded(EMPTY_BLAMED);
    }
  }

  private BlameAnnotation triggerReload(@NotNull VirtualFile file) {
    tryTaskSubmission(file);
    return BlameAnnotation.EMPTY;
  }

  private void tryTaskSubmission(@NotNull VirtualFile file) {
    if (queued.add(file)) {
      LOG.debug("Add annotation task for ", file);
      annotations.put(file, CachedFactory.loading(EMPTY_BLAMED));
      AnnotationLoader loaderTask = new AnnotationLoader(
          file, blameLoader, loaderTimers, this::annotationLoaded);
      executor.execute(loaderTask);
    } else {
      LOG.debug("Discard annotation task for ", file);
      discardedSubmitCounter.inc();
    }
  }

  private boolean isChanged(@NotNull VirtualFile file, @NotNull BlameAnnotation annotation) {
    VcsRevisionNumber currentRevision = currentCurrentRevision(file);
    return annotation.isChanged(currentRevision);
  }

  private void annotationLoaded(@NotNull VirtualFile file, @NotNull BlameAnnotation annotation) {
    if (queued.remove(file)) {
      annotations.put(file, CachedFactory.loaded(new Blamed(annotation)));
      gateway.fireBlameUpdated(file, annotation);
    }
  }

  @NotNull
  private VcsRevisionNumber currentCurrentRevision(@NotNull VirtualFile file) {
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
  public void refreshForRoot(@NotNull VirtualFile root) {
    LOG.debug("Refresh for root: ", root);
    Set<VirtualFile> files = new HashSet<>(annotations.asMap().keySet());
    files.stream()
        .filter(file -> VfsUtilCore.isAncestor(root, file, false))
        .forEach(this::markDirty);
  }

  private void markDirty(@NotNull VirtualFile file) {
    Cached<Blamed> cached = annotations.getIfPresent(file);
    if (cached != null && !cached.isLoading() && !cached.isEmpty()) {
      LOG.debug("Mark dirty ", file);
      cached.value().markDirty();
    }
  }

  private static class Blamed {
    private final BlameAnnotation annotation;
    private final AtomicBoolean dirty = new AtomicBoolean();

    private Blamed(BlameAnnotation annotation) {
      this.annotation = annotation;
    }

    private boolean checkDirty() {
      return dirty.compareAndSet(true, false);
    }

    private void markDirty() {
      dirty.compareAndSet(false, true);
    }
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
