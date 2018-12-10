package zielu.gittoolbox.blame;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Timer;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import git4idea.repo.GitRepository;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.cache.VirtualFileRepoCache;
import zielu.gittoolbox.metrics.ProjectMetrics;

class BlameCacheImpl implements BlameCache {
  private static final BlameAnnotation EMPTY = new BlameAnnotation() {
    @Nullable
    @Override
    public Blame getBlame(int lineNumber) {
      return null;
    }

    @Override
    public boolean isChanged(@NotNull VcsRevisionNumber revision) {
      return !VcsRevisionNumber.NULL.equals(revision);
    }

    @Nullable
    @Override
    public VirtualFile getVirtualFile() {
      return null;
    }
  };
  private static final Logger LOG = Logger.getInstance(BlameCacheImpl.class);
  private final Project project;
  private final VirtualFileRepoCache fileRepoCache;
  private final BlameLoader blameLoader;
  private final Map<VirtualFile, BlameAnnotation> annotations = new ConcurrentHashMap<>();
  private final Set<VirtualFile> queued = ContainerUtil.newConcurrentSet();
  private final Timer getTimer;
  private final Timer loadTimer;
  private final Timer queueWaitTimer;
  private final Counter discardedSubmitCounter;

  private ExecutorService executor;

  BlameCacheImpl(@NotNull Project project, @NotNull VirtualFileRepoCache fileRepoCache,
                 @NotNull BlameLoader blameLoader, @NotNull ProjectMetrics metrics) {
    this.project = project;
    this.fileRepoCache = fileRepoCache;
    this.blameLoader = blameLoader;
    executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
        .setDaemon(true)
        .setNameFormat("blame-cache-%d")
        .build()
    );
    metrics.gauge("blame-cache-size", annotations::size);
    metrics.gauge("blame-cache-queue-count", queued::size);
    discardedSubmitCounter = metrics.counter("blame-cache-discarded-count");
    getTimer = metrics.timer("blame-cache-get");
    loadTimer = metrics.timer("blame-cache-load");
    queueWaitTimer = metrics.timer("blame-cache-queue-wait");
  }

  @NotNull
  @Override
  public BlameAnnotation getAnnotation(@NotNull VirtualFile file) {
    return getTimer.timeSupplier(() -> getAnnotationInternal(file));
  }

  private BlameAnnotation getAnnotationInternal(@NotNull VirtualFile file) {
    return annotations.compute(file, (fileKey, existingAnnotation) -> {
      if (existingAnnotation == null) {
        submitTask(fileKey);
        return EMPTY;
      } else {
        return handleExistingAnnotation(fileKey, existingAnnotation);
      }
    });
  }

  private void submitTask(@NotNull VirtualFile file) {
    if (queued.add(file)) {
      LOG.debug("Add annotation task for ", file);
      executor.execute(new FileAnnotationLoader(file, blameLoader, loaderTimers(), this::updateAnnotation));
    } else {
      LOG.debug("Discard annotation task for ", file);
      discardedSubmitCounter.inc();
    }
  }

  private LoaderTimers loaderTimers() {
    return new LoaderTimers(loadTimer, queueWaitTimer);
  }

  @NotNull
  private BlameAnnotation handleExistingAnnotation(@NotNull VirtualFile file, @NotNull BlameAnnotation annotation) {
    if (isChanged(file, annotation)) {
      submitTask(file);
      return EMPTY;
    } else {
      return annotation;
    }
  }

  private boolean isChanged(@NotNull VirtualFile file, @NotNull BlameAnnotation annotation) {
    VcsRevisionNumber currentRevision = currentRepoRevision(file);
    return annotation.isChanged(currentRevision);
  }

  private void updateAnnotation(@NotNull VirtualFile file, @Nullable FileAnnotation fileAnnotation) {
    if (queued.remove(file)) {
      BlameAnnotation blameAnnotation;
      if (fileAnnotation != null) {
        blameAnnotation = new BlameAnnotationImpl(fileAnnotation);
      } else {
        blameAnnotation = EMPTY;
      }
      annotations.put(file, blameAnnotation);
      fireUpdated(file, blameAnnotation);
    }
  }

  private void fireUpdated(@NotNull VirtualFile file, @NotNull BlameAnnotation annotation) {
    project.getMessageBus().syncPublisher(BlameCache.TOPIC).cacheUpdated(file, annotation);
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
    annotations.remove(file);
  }

  private static class LoaderTimers {
    final Timer load;
    final Timer queueWait;

    private LoaderTimers(Timer load, Timer queueWait) {
      this.load = load;
      this.queueWait = queueWait;
    }
  }

  private static class FileAnnotationLoader implements Runnable {
    private final VirtualFile file;
    private final BlameLoader loader;
    private final BiConsumer<VirtualFile, FileAnnotation> loaded;
    private final LoaderTimers timers;
    private final long createdAt = System.currentTimeMillis();

    private FileAnnotationLoader(@NotNull VirtualFile file, @NotNull BlameLoader loader, @NotNull LoaderTimers timers,
                                 @NotNull BiConsumer<VirtualFile, FileAnnotation> loaded) {
      this.file = file;
      this.loader = loader;
      this.timers = timers;
      this.loaded = loaded;
    }

    @Override
    public void run() {
      timers.queueWait.update(System.currentTimeMillis() - createdAt, TimeUnit.MILLISECONDS);
      FileAnnotation annotation = timers.load.timeSupplier(this::load);
      LOG.info("Annotated " + file + ": " + annotation);
      loaded.accept(file, annotation);
    }

    @Nullable
    private FileAnnotation load() {
      LOG.debug("Annotate ", file);
      try {
        return loader.annotate(file);
      } catch (VcsException e) {
        LOG.warn("Failed to annotate " + file, e);
        return null;
      }
    }
  }
}
