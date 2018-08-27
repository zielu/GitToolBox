package zielu.gittoolbox.cache;

import com.codahale.metrics.Counter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
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
import java.util.Objects;
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
import zielu.gittoolbox.util.GtUtil;

class PerRepoInfoCacheImpl implements ProjectComponent, PerRepoInfoCache {
  private final Logger log = Logger.getInstance(getClass());
  private final AtomicBoolean active = new AtomicBoolean();
  private final ConcurrentMap<GitRepository, RepoInfo> behindStatuses = Maps.newConcurrentMap();
  private final Multimap<GitRepository, CacheTask> scheduledRepositories = HashMultimap.create();
  private final CachedStatusCalculator statusCalculator = new CachedStatusCalculator();
  private final Project project;
  private final GitStatusCalculator calculator;
  private final Counter statusQueueSize;
  private final Counter discardedTasksCount;
  private ExecutorService updateExecutor;
  private MessageBus messageBus;

  PerRepoInfoCacheImpl(@NotNull Project project) {
    this.project = project;
    calculator = GitStatusCalculator.create(project);
    Metrics metrics = MetricsHost.project(project);
    metrics.gauge("info-cache-size", behindStatuses::size);
    statusQueueSize = metrics.counter("info-cache-queue-size");
    discardedTasksCount = metrics.counter("info-cache-discarded-updates");
  }

  @Override
  public void initComponent() {
    messageBus = project.getMessageBus();
  }

  private void update(@NotNull GitRepository repository) {
    if (active.get()) {
      updateRepositoryStatus(repository);
    }
  }

  private void updateRepositoryStatus(@NotNull GitRepository repository) {
    DumbService dumbService = DumbService.getInstance(project);
    Runnable update = () -> updateAction(repository);
    dumbService.runReadActionInSmartMode(update);
  }

  private void updateAction(@NotNull GitRepository repository) {
    RepoInfo info = getRepoInfo(repository);
    RepoStatus currentStatus = RepoStatus.create(repository);
    if (!Objects.equals(info.status(), currentStatus)) {
      RepoInfo freshInfo = behindStatuses.computeIfPresent(repository, (repo, oldInfo) ->
          statusCalculator.update(repo, calculator, currentStatus));
      onRepoChanged(repository, freshInfo);
    } else {
      log.debug("Status did not change [", GtUtil.name(repository), "]");
    }
  }

  @NotNull
  @Override
  public RepoInfo getInfo(@NotNull GitRepository repository) {
    RepoInfo repoInfo = getRepoInfo(repository);
    if (repoInfo.isEmpty()) {
      scheduleUpdate(repository);
    }
    return repoInfo;
  }

  @NotNull
  private RepoInfo getRepoInfo(@NotNull GitRepository repository) {
    return behindStatuses.computeIfAbsent(repository, repo -> RepoInfo.empty());
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
      scheduleTask(new RefreshTask(repository), false);
    } else {
      log.debug("Inactive - ignored scheduling refresh for: ", repository);
    }
  }

  private void scheduleMandatoryRefresh(@NotNull GitRepository repository) {
    if (active.get()) {
      scheduleTask(new RefreshTask(repository), true);
    } else {
      log.debug("Inactive - ignored scheduling refresh for: ", repository);
    }
  }

  private void scheduleUpdate(@NotNull GitRepository repository) {
    if (active.get()) {
      scheduleTask(new UpdateTask(repository), false);
    } else {
      log.debug("Inactive - ignored updating refresh for ", repository);
    }
  }

  private synchronized void scheduleTask(CacheTask task, boolean mandatory) {
    boolean canSubmit;
    if (mandatory) {
      scheduledRepositories.put(task.repository, task);
      canSubmit = true;
    } else {
      Collection<CacheTask> alreadyScheduled = scheduledRepositories.get(task.repository);
      if (alreadyScheduled.isEmpty()) {
        scheduledRepositories.put(task.repository, task);
        canSubmit = true;
      } else {
        log.debug("Tasks for ", task.repository, " already scheduled: ", alreadyScheduled);
        canSubmit = false;
      }
    }

    if (canSubmit) {
      submitForExecution(task);
    } else {
      discardedTasksCount.inc();
    }
  }

  private void submitForExecution(CacheTask  task) {
    updateExecutor.submit(task);
    statusQueueSize.inc();
    log.debug("Scheduled: ", task);
  }

  @Override
  public void repoChanged(@NotNull GitRepository repository) {
    log.debug("Got repo changed event: ", repository);
    scheduleMandatoryRefresh(repository);
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

  private synchronized void removeRepository(@NotNull GitRepository repository) {
    behindStatuses.remove(repository);
    scheduledRepositories.removeAll(repository).forEach(CacheTask::kill);
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
      updateExecutor = createExecutor();
    }
  }

  private ExecutorService createExecutor() {
    ThreadFactoryBuilder threadBuilder = new ThreadFactoryBuilder()
        .setDaemon(true)
        .setNameFormat(getClass().getSimpleName() + "-[" + project.getName() + "]-%d");
    return Executors.newSingleThreadExecutor(threadBuilder.build());
  }

  @Override
  public void projectClosed() {
    if (active.compareAndSet(true, false)) {
      destroyExecutor();
    }
  }

  private void destroyExecutor() {
    List<Runnable> notStartedTasks = updateExecutor.shutdownNow();
    statusQueueSize.dec(notStartedTasks.size());
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
          synchronized (PerRepoInfoCacheImpl.this) {
            scheduledRepositories.remove(repository, this);
          }
          runImpl();
        }
      } finally {
        statusQueueSize.dec();
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
