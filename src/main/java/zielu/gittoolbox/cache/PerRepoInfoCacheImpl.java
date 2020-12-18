package zielu.gittoolbox.cache;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import git4idea.repo.GitRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.metrics.ProjectMetrics;
import zielu.gittoolbox.status.GitStatusCalculator;
import zielu.gittoolbox.util.GtUtil;
import zielu.gittoolbox.util.MemoizeSupplier;
import zielu.intellij.util.ZDisposeGuard;

class PerRepoInfoCacheImpl implements PerRepoInfoCache, Disposable {
  private final Logger log = Logger.getInstance(getClass());
  private final ZDisposeGuard disposeGuard = new ZDisposeGuard();
  private final Supplier<ConcurrentMap<GitRepository, RepoInfo>> behindStatuses;
  private final CachedStatusCalculator statusCalculator;
  private final Project project;
  private final GitStatusCalculator calculator;
  private final InfoCachePublisher publisher;

  PerRepoInfoCacheImpl(@NotNull Project project) {
    this.project = project;
    behindStatuses = new MemoizeSupplier<>(this::createBehindStatuses);
    statusCalculator = new CachedStatusCalculator(() -> ProjectMetrics.getInstance(project));
    calculator = GitStatusCalculator.create(project);
    publisher = new InfoCachePublisher(project);
    publisher.registerDisposable(this, publisher);
    publisher.registerDisposable(this, disposeGuard);
  }

  private ConcurrentMap<GitRepository, RepoInfo> createBehindStatuses() {
    ConcurrentMap<GitRepository, RepoInfo> storage = Maps.newConcurrentMap();
    ProjectMetrics.getInstance(project).gauge("info-cache.size", storage::size);
    return storage;
  }

  private void update(@NotNull GitRepository repository) {
    if (disposeGuard.isActive()) {
      updateRepositoryStatus(repository);
    }
  }

  private void updateRepositoryStatus(@NotNull GitRepository repository) {
    updateAction(repository);
  }

  private void updateAction(@NotNull GitRepository repository) {
    RepoInfo info = getRepoInfo(repository);
    InfoCacheGateway gateway = InfoCacheGateway.getInstance(project);
    RepoStatus currentStatus = gateway.createRepoStatus(repository);
    RepoInfo freshInfo = behindStatuses.get().computeIfPresent(repository, (repo, oldInfo) ->
        statusCalculator.update(repo, calculator, currentStatus));

    if (freshInfo != null && !Objects.equals(info, freshInfo)) {
      publisher.notifyRepoChanged(repository, info, freshInfo);
    } else {
      log.debug("Status did not change [", GtUtil.name(repository), "]: ", freshInfo);
    }
  }

  @NotNull
  @Override
  public RepoInfo getInfo(@NotNull GitRepository repository) {
    if (disposeGuard.isActive()) {
      RepoInfo repoInfo = getRepoInfo(repository);
      if (repoInfo.isEmpty()) {
        scheduleUpdate(repository);
      }
      return repoInfo;
    }
    return RepoInfo.empty();
  }

  @NotNull
  private RepoInfo getRepoInfo(@NotNull GitRepository repository) {
    return behindStatuses.get().computeIfAbsent(repository, repo -> RepoInfo.empty());
  }

  @NotNull
  @Override
  public List<RepoInfo> getAllInfos() {
    return new ArrayList<>(behindStatuses.get().values());
  }

  @Override
  public void dispose() {
    behindStatuses.get().clear();
  }

  private void scheduleMandatoryRefresh(@NotNull GitRepository repository) {
    CacheTaskScheduler.getInstance(project).scheduleMandatory(repository, new RefreshTask());
  }

  private void scheduleUpdate(@NotNull GitRepository repository) {
    CacheTaskScheduler.getInstance(project).scheduleOptional(repository, new UpdateTask());
  }

  @Override
  public void repoChanged(@NotNull GitRepository repository) {
    if (disposeGuard.isActive()) {
      log.debug("Got repo changed event: ", repository);
      scheduleMandatoryRefresh(repository);
    }
  }

  @Override
  public void updatedRepoList(ImmutableList<GitRepository> repositories) {
    if (disposeGuard.isActive()) {
      Set<GitRepository> removed = new HashSet<>(behindStatuses.get().keySet());
      removed.removeAll(repositories);
      purgeRepositories(removed);
    }
  }

  private void purgeRepositories(@NotNull Collection<GitRepository> repositories) {
    removeRepositories(repositories);
    publisher.notifyEvicted(repositories);
  }

  private void removeRepositories(@NotNull Collection<GitRepository> repositories) {
    repositories.forEach(this::removeRepository);
  }

  private void removeRepository(@NotNull GitRepository repository) {
    CacheTaskScheduler.getInstance(project).removeRepository(repository);
    behindStatuses.get().remove(repository);
  }

  @Override
  public void refreshAll() {
    if (disposeGuard.isActive()) {
      log.debug("Refreshing all repository statuses");
      refresh(GtUtil.getRepositories(project));
    }
  }

  @Override
  public void refresh(@NotNull Iterable<? extends GitRepository> repositories) {
    if (disposeGuard.isActive()) {
      log.debug("Refreshing repositories statuses: ", repositories);
      repositories.forEach(this::scheduleUpdate);
    }
  }

  private class RefreshTask implements CacheTaskScheduler.Task {
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
