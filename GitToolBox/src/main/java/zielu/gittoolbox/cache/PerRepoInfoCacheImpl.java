package zielu.gittoolbox.cache;

import com.codahale.metrics.Counter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import git4idea.GitUtil;
import git4idea.repo.GitRepository;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.metrics.Metrics;
import zielu.gittoolbox.metrics.MetricsHost;
import zielu.gittoolbox.status.GitStatusCalculator;

class PerRepoInfoCacheImpl implements ProjectComponent, PerRepoInfoCache {
  private final Logger log = Logger.getInstance(getClass());
  private final AtomicBoolean active = new AtomicBoolean();
  private final ConcurrentMap<GitRepository, CachedStatus> behindStatuses = Maps.newConcurrentMap();
  private final ConcurrentMap<GitRepository, CacheTask> scheduledRepositories = Maps.newConcurrentMap();
  private final Project project;
  private final GitStatusCalculator calculator;
  private final Counter behindStatusQueueSize;
  private ExecutorService updateExecutor;
  private MessageBus messageBus;

  PerRepoInfoCacheImpl(@NotNull Project project) {
    this.project = project;
    calculator = GitStatusCalculator.create(project);
    Metrics metrics = MetricsHost.app();
    metrics.gauge("behind-status-cache-size", behindStatuses::size);
    behindStatusQueueSize = metrics.counter("behind-status-queue-size");
  }

  @Override
  public void initComponent() {
    messageBus = project.getMessageBus();
  }

  @NotNull
  private CachedStatus get(@NotNull GitRepository repository) {
    CachedStatus cachedStatus = behindStatuses.get(repository);
    if (cachedStatus == null) {
      cachedStatus = prepareCachedStatus(repository);
    }
    return cachedStatus;
  }

  private CachedStatus prepareCachedStatus(@NotNull GitRepository repository) {
    CachedStatus status = createCachedStatus(repository);
    if (status.isNew()) {
      scheduleUpdate(repository);
    }
    return status;
  }

  private CachedStatus createCachedStatus(@NotNull GitRepository repository) {
    CachedStatus newStatus = CachedStatus.create(repository);
    CachedStatus foundStatus = behindStatuses.putIfAbsent(repository, newStatus);
    return foundStatus != null ? foundStatus : newStatus;
  }

  private void update(@NotNull GitRepository repository) {
    if (active.get()) {
      updateRepositoryStatus(repository);
    }
  }

  private void updateRepositoryStatus(@NotNull GitRepository repository) {
    DumbService dumbService = DumbService.getInstance(project);
    Runnable update = () -> get(repository).update(repository, calculator, info -> onRepoChanged(repository, info));
    dumbService.runReadActionInSmartMode(update);
  }

  @NotNull
  @Override
  public RepoInfo getInfo(GitRepository repository) {
    CachedStatus cachedStatus = get(repository);
    return cachedStatus.get();
  }

  @Override
  public void disposeComponent() {
    messageBus = null;
    behindStatuses.clear();
    updateExecutor = null;
    scheduledRepositories.clear();
  }

  private void scheduleRefresh(@NotNull GitRepository repository) {
    if (active.get()) {
      scheduleTask(new RefreshTask(repository));
    } else {
      log.debug("Inactive - ignored scheduling refresh for: ", repository);
    }
  }

  private void scheduleUpdate(@NotNull GitRepository repository) {
    if (active.get()) {
      scheduleTask(new UpdateTask(repository));
    } else {
      log.debug("Inactive - ignored updating refresh for ", repository);
    }
  }

  private void scheduleTask(CacheTask task) {
    CacheTask alreadyScheduled = scheduledRepositories.putIfAbsent(task.repository, task);
    if (alreadyScheduled == null) {
      submitForExecution(task);
    } else {
      log.debug("Task for ", task.repository, " already scheduled: ", alreadyScheduled);
    }
  }

  private void submitForExecution(CacheTask  task) {
    updateExecutor.submit(task);
    behindStatusQueueSize.inc();
    log.debug("Scheduled: ", task);
  }

  @Override
  public void repoChanged(@NotNull GitRepository repository) {
    log.debug("Got repo changed event: ", repository);
    scheduleRefresh(repository);
  }

  @Override
  public void updatedRepoList(ImmutableList<GitRepository> repositories) {
    Set<GitRepository> removed = new HashSet<>(behindStatuses.keySet());
    removed.removeAll(repositories);
    purgeRepositories(removed);
  }

  private void purgeRepositories(@NotNull Collection<GitRepository> repositories) {
    removeRepositories(repositories);
    notifyEvicted(repositories);
  }

  private void removeRepositories(@NotNull Collection<GitRepository> repositories) {
    repositories.forEach(this::removeRepository);
  }

  private void removeRepository(@NotNull GitRepository repository) {
    behindStatuses.remove(repository);
    Optional.ofNullable(scheduledRepositories.remove(repository)).ifPresent(CacheTask::kill);
  }

  private void notifyEvicted(@NotNull Collection<GitRepository> repositories) {
    messageBus.syncPublisher(CACHE_CHANGE).evicted(repositories);
  }

  private void onRepoChanged(GitRepository repo, RepoInfo info) {
    if (active.get()) {
      messageBus.syncPublisher(CACHE_CHANGE).stateChanged(info, repo);
      log.debug("Published cache changed event: ", repo);
    }
  }

  private void refreshSync(GitRepository repository) {
    get(repository).invalidate();
    update(repository);
  }

  private void updateSync(GitRepository repository) {
    update(repository);
  }

  @Override
  public void refreshAll() {
    log.debug("Refreshing all repository statuses");
    refresh(GitUtil.getRepositories(project));
  }

  @Override
  public void refresh(Iterable<GitRepository> repositories) {
    log.debug("Refreshing repositories statuses: ", repositories);
    repositories.forEach(this::scheduleRefresh);
  }

  @Override
  public void projectOpened() {
    if (active.compareAndSet(false, true)) {
      createExecutor();
    }
  }

  private void createExecutor() {
    ThreadFactoryBuilder threadBuilder = new ThreadFactoryBuilder().setDaemon(true);
    updateExecutor = Executors.newSingleThreadExecutor(
        threadBuilder.setNameFormat(getClass().getSimpleName() + "-[" + project.getName() + "]-%d").build()
    );
  }

  @Override
  public void projectClosed() {
    if (active.compareAndSet(true, false)) {
      destroyExecutor();
    }
  }

  private void destroyExecutor() {
    List<Runnable> notStartedTasks = updateExecutor.shutdownNow();
    behindStatusQueueSize.dec(notStartedTasks.size());
    notStartedTasks.forEach(notStarted -> log.info("Task " + notStarted + " was never started"));
  }

  private class RefreshTask extends CacheTask {
    private RefreshTask(@NotNull GitRepository repository) {
      super(repository);
    }

    @Override
    public void runImpl() {
      refreshSync(repository);
    }

    @Override
    public String toString() {
      return "RefreshTask for " + repository;
    }
  }

  private class UpdateTask extends CacheTask {
    private UpdateTask(@NotNull GitRepository repository) {
      super(repository);
    }

    @Override
    public void runImpl() {
      updateSync(repository);
    }

    @Override
    public String toString() {
      return "UpdateTask for " + repository;
    }
  }

  private abstract class CacheTask implements Runnable {
    private final AtomicBoolean taskActive = new AtomicBoolean(true);
    final GitRepository repository;

    CacheTask(GitRepository repository) {
      this.repository = repository;
    }

    void kill() {
      taskActive.set(false);
    }

    @Override
    public void run() {
      try {
        if (active.get() && taskActive.get()) {
          runImpl();
          scheduledRepositories.remove(repository);
        }
      } finally {
        behindStatusQueueSize.dec();
      }
    }

    abstract void runImpl();

    @Override
    public String toString() {
      return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
          .append("repository", repository)
          .toString();
    }
  }
}
