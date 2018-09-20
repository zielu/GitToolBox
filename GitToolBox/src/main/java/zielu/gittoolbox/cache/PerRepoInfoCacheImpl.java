package zielu.gittoolbox.cache;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import git4idea.GitUtil;
import git4idea.repo.GitRepository;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
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
  private final CachedStatusCalculator statusCalculator = new CachedStatusCalculator();
  private final Project project;
  private final GitStatusCalculator calculator;
  private final CacheTaskScheduler taskScheduler;
  private final InfoCachePublisher publisher;

  PerRepoInfoCacheImpl(@NotNull Project project, @NotNull InfoCachePublisher publisher) {
    this.project = project;
    this.publisher = publisher;
    calculator = GitStatusCalculator.create(project);
    Metrics metrics = MetricsHost.project(project);
    taskScheduler = new CacheTaskScheduler(project, metrics);
    metrics.gauge("info-cache-size", behindStatuses::size);
  }

  @Override
  public void initComponent() {
    taskScheduler.initialize();
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
    RepoInfo freshInfo = behindStatuses.computeIfPresent(repository, (repo, oldInfo) ->
        statusCalculator.update(repo, calculator, currentStatus));

    if (!Objects.equals(info, freshInfo)) {
      publisher.notifyRepoChanged(repository, freshInfo);
    } else {
      log.debug("Status did not change [", GtUtil.name(repository), "]: ", freshInfo);
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
    taskScheduler.dispose();
    behindStatuses.clear();
  }

  private void scheduleRefresh(@NotNull GitRepository repository) {
    taskScheduler.scheduleOptional(repository, new RefreshTask());
  }

  private void scheduleMandatoryRefresh(@NotNull GitRepository repository) {
    taskScheduler.scheduleMandatory(repository, new RefreshTask());
  }

  private void scheduleUpdate(@NotNull GitRepository repository) {
    taskScheduler.scheduleOptional(repository, new UpdateTask());
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
    publisher.notifyEvicted(repositories);
  }

  private void removeRepositories(@NotNull Collection<GitRepository> repositories) {
    repositories.forEach(this::removeRepository);
  }

  private void removeRepository(@NotNull GitRepository repository) {
    taskScheduler.removeRepository(repository);
    behindStatuses.remove(repository);
  }

  private void refreshRepo(GitRepository repository) {
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
      taskScheduler.opened();
    }
  }

  @Override
  public void projectClosed() {
    if (active.compareAndSet(true, false)) {
      taskScheduler.closed();
    }
  }

  private class RefreshTask implements CacheTaskScheduler.Task {
    @Override
    public void run(@NotNull GitRepository repository) {
      refreshRepo(repository);
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
          .build();
    }
  }

  private class UpdateTask implements CacheTaskScheduler.Task {
    @Override
    public void run(@NotNull GitRepository repository) {
      update(repository);
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
          .build();
    }
  }
}
