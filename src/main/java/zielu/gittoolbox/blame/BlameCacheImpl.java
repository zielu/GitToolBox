package zielu.gittoolbox.blame;

import com.google.common.base.Supplier;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import git4idea.repo.GitRepository;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.util.Cached;
import zielu.gittoolbox.util.CachedFactory;
import zielu.gittoolbox.util.DisposeAfterExecutableTask;
import zielu.gittoolbox.util.DisposeSafeExecutableTask;
import zielu.gittoolbox.util.ExecutableTask;
import zielu.intellij.util.ZDisposeGuard;

class BlameCacheImpl implements BlameCache, Disposable {
  private static final Logger LOG = Logger.getInstance(BlameCacheImpl.class);
  private final ZDisposeGuard disposeGuard = new ZDisposeGuard();
  private final BlameCacheFacade facade;
  private final LoadingCache<VirtualFile, Cached<Blamed>> annotations;
  private final Set<VirtualFile> queued = ContainerUtil.newConcurrentSet();

  BlameCacheImpl(@NotNull Project project) {
    facade = new BlameCacheFacade(project);
    annotations = CacheBuilder.newBuilder()
                      .weakKeys()
                      .maximumSize(facade.maxEntries())
                      .expireAfterAccess(Duration.ofMinutes(45))
                      .recordStats()
                      .build(CacheLoader.from((Supplier<Cached<Blamed>>) CachedFactory::loading));
    facade.exposeCacheMetrics(annotations, "blame-cache");
    facade.registerQueuedGauge(queued::size);
    facade.registerDisposable(this, facade);
    facade.registerDisposable(this, disposeGuard);
  }

  @Override
  public void dispose() {
    annotations.invalidateAll();
    queued.clear();
  }

  @NotNull
  @Override
  public BlameAnnotation getAnnotation(@NotNull VirtualFile file) {
    if (disposeGuard.isActive()) {
      return facade.timeCacheGet(() -> getAnnotationInternal(file));
    }
    return BlameAnnotation.EMPTY;
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
    return annotations.getUnchecked(file);
  }

  private BlameAnnotation triggerReload(@NotNull VirtualFile file) {
    tryTaskSubmission(file);
    return BlameAnnotation.EMPTY;
  }

  private void tryTaskSubmission(@NotNull VirtualFile file) {
    if (queued.add(file)) {
      LOG.debug("Add annotation task for ", file);
      annotations.put(file, CachedFactory.loading(new Blamed(BlameAnnotation.EMPTY)));
      AnnotationLoader loaderTask = new AnnotationLoader(file, facade, this::annotationLoaded);
      facade.registerDisposable(this, loaderTask);
      facade.execute(new DisposeSafeExecutableTask(new DisposeAfterExecutableTask(loaderTask, loaderTask)));
    } else {
      LOG.debug("Discard annotation task for ", file);
      facade.submitDiscarded();
    }
  }

  private boolean isChanged(@NotNull VirtualFile file, @NotNull BlameAnnotation annotation) {
    VcsRevisionNumber currentRevision = currentRepoRevision(file);
    return annotation.isChanged(currentRevision);
  }

  private void annotationLoaded(@NotNull VirtualFile file, @NotNull BlameAnnotation annotation) {
    if (disposeGuard.isActive() && queued.remove(file)) {
      annotations.put(file, CachedFactory.loaded(new Blamed(annotation)));
      facade.fireBlameUpdated(file, annotation);
    }
  }

  @NotNull
  private VcsRevisionNumber currentRepoRevision(@NotNull VirtualFile file) {
    GitRepository repo = facade.getRepoForFile(file);
    if (repo != null) {
      return facade.getCurrentRevision(repo);
    }
    return VcsRevisionNumber.NULL;
  }

  @Override
  public void refreshForRoot(@NotNull VirtualFile root) {
    if (disposeGuard.isActive()) {
      refreshForRootImpl(root);
    }
  }

  private void refreshForRootImpl(@NotNull VirtualFile root) {
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
      cached.value()
          .markDirty();
    }
  }

  @Override
  public void invalidateForRoot(@NotNull VirtualFile root) {
    if (disposeGuard.isActive()) {
      invalidateForRootImpl(root);
    }
  }

  private void invalidateForRootImpl(@NotNull VirtualFile root) {
    LOG.debug("Invalidate for root: ", root);
    facade.invalidateForRoot(root);
    Set<VirtualFile> files = new HashSet<>(annotations.asMap()
        .keySet());
    files.stream()
        .filter(file -> VfsUtilCore.isAncestor(root, file, false))
        .forEach(this::invalidate);
  }

  private void invalidate(@NotNull VirtualFile file) {
    annotations.invalidate(file);
    facade.fireBlameInvalidated(file);
    LOG.debug("Invalidated: ", file);
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

  private static class AnnotationLoader implements ExecutableTask, Disposable {
    private final ZDisposeGuard disposeGuard = new ZDisposeGuard();
    private final VirtualFile file;
    private final BlameCacheFacade gateway;
    private final BiConsumer<VirtualFile, BlameAnnotation> loaded;
    private final long createdAt = System.currentTimeMillis();

    private AnnotationLoader(@NotNull VirtualFile file, @NotNull BlameCacheFacade gateway,
                             @NotNull BiConsumer<VirtualFile, BlameAnnotation> loaded) {
      this.file = file;
      this.gateway = gateway;
      this.loaded = loaded;
    }

    @Override
    public void run() {
      gateway.updateQueueWait(System.currentTimeMillis() - createdAt, TimeUnit.MILLISECONDS);
      disposeGuard.checkAndThrow();
      BlameAnnotation annotation = gateway.timeLoad(this::load);
      LOG.info("Annotated " + file + ": " + annotation);
      disposeGuard.checkAndThrow();
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
        return gateway.getBlameLoader().annotate(file);
      } catch (Exception e) {
        LOG.warn("Failed to annotate " + file, e);
        return BlameAnnotation.EMPTY;
      }
    }

    @Override
    public void dispose() {
      gateway.dispose(disposeGuard);
    }
  }
}
