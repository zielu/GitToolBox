package zielu.gittoolbox.cache;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.messages.Topic;
import git4idea.GitUtil;
import git4idea.repo.GitRepository;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.status.GitStatusCalculator;

public class PerRepoInfoCacheImpl implements ProjectComponent, PerRepoInfoCache {
  public static final Topic<PerRepoStatusCacheListener> CACHE_CHANGE = Topic.create("Status cache change",
      PerRepoStatusCacheListener.class);

  private final Logger log = Logger.getInstance(getClass());
  private final AtomicBoolean active = new AtomicBoolean();
  private final ConcurrentMap<GitRepository, CachedStatus> behindStatuses = Maps.newConcurrentMap();
  private final ConcurrentMap<GitRepository, CacheTask> scheduledRepositories = Maps.newConcurrentMap();
  private final Project project;
  private final GitStatusCalculator calculator;
  private MessageBusConnection connection;
  private ExecutorService updateExecutor;

  public PerRepoInfoCacheImpl(@NotNull Project project) {
    this.project = project;
    calculator = GitStatusCalculator.create(project);
  }

  @Override
  public void initComponent() {
    connection = project.getMessageBus().connect();
    connection.subscribe(CacheSubscriber.SUBSCRIBER_CHANGE, new CacheSubscriptionListener() {
      @Override
      public void repoChanged(@NotNull GitRepository repository) {
        repositoryChanged(repository);
      }

      @Override
      public void dirMappingChanged(ImmutableList<GitRepository> repositories) {
        directoryMappingChanged(repositories);
      }
    });
  }

  private CachedStatus get(GitRepository repository) {
    CachedStatus cachedStatus = behindStatuses.get(repository);
    if (cachedStatus == null) {
      CachedStatus newStatus = CachedStatus.create(repository);
      CachedStatus foundStatus = behindStatuses.putIfAbsent(repository, newStatus);
      cachedStatus = foundStatus != null ? foundStatus : newStatus;
      if (cachedStatus.isNew()) {
        scheduleUpdate(repository);
      }
    }
    return cachedStatus;
  }

  private void update(GitRepository repository) {
    if (active.get()) {
      DumbService dumbService = DumbService.getInstance(project);
      Runnable update = () -> get(repository).update(repository, calculator, info -> onRepoChanged(repository, info));
      dumbService.runReadActionInSmartMode(update);
    }
  }

  @NotNull
  @Override
  public RepoInfo getInfo(GitRepository repository) {
    CachedStatus cachedStatus = get(repository);
    return cachedStatus.get();
  }

  @Override
  public void disposeComponent() {
    connection.disconnect();
    connection = null;
    behindStatuses.clear();
    updateExecutor = null;
    scheduledRepositories.clear();
  }

  private void scheduleRefresh(@NotNull GitRepository repository) {
    if (active.get()) {
      log.debug("Scheduled refresh for: ", repository);
      scheduleTask(new RefreshTask(repository));
    } else {
      log.debug("Inactive - ignored scheduling refresh for ", repository);
    }
  }

  private void scheduleUpdate(@NotNull GitRepository repository) {
    if (active.get()) {
      log.debug("Scheduled update for: ", repository);
      scheduleTask(new UpdateTask(repository));
    } else {
      log.debug("Inactive - ignored updating refresh for ", repository);
    }
  }

  private void scheduleTask(CacheTask task) {
    if (scheduledRepositories.putIfAbsent(task.repository, task) == null) {
      updateExecutor.submit(task);
      log.debug("Scheduled ", task);
    } else {
      log.debug("Task for ", task.repository, " already scheduled");
    }
  }

  private void repositoryChanged(@NotNull GitRepository repository) {
    log.debug("Got repo changed event: ", repository);
    scheduleRefresh(repository);
  }

  private void directoryMappingChanged(ImmutableList<GitRepository> repositories) {
    Set<GitRepository> removed = new HashSet<>(behindStatuses.keySet());
    removed.removeAll(repositories);
    removed.forEach(removedRepo -> {
      behindStatuses.remove(removedRepo);
      Optional.ofNullable(scheduledRepositories.remove(removedRepo)).ifPresent(CacheTask::kill);
    });
    project.getMessageBus().syncPublisher(CACHE_CHANGE).evicted(removed);
  }

  private void onRepoChanged(GitRepository repo, RepoInfo info) {
    if (active.get()) {
      project.getMessageBus().syncPublisher(CACHE_CHANGE).stateChanged(info, repo);
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
      ThreadFactoryBuilder threadBuilder = new ThreadFactoryBuilder().setDaemon(true);
      updateExecutor = Executors.newSingleThreadExecutor(
          threadBuilder.setNameFormat(getClass().getSimpleName() + "-[" + project.getName() + "]-%d").build()
      );
    }
  }

  @Override
  public void projectClosed() {
    if (active.compareAndSet(true, false)) {
      updateExecutor.shutdownNow().forEach(notStarted -> log.info("Task " + notStarted + " was never started"));
    }
  }

  private class RefreshTask extends CacheTask {
    private RefreshTask(@NotNull GitRepository repository) {
      super(repository);
    }

    @Override
    public void runImpl() {
      refreshSync(repository);
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
      if (active.get() && taskActive.get()) {
        runImpl();
        scheduledRepositories.remove(repository);
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
