package zielu.gittoolbox.fetch;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task.Backgroundable;
import com.intellij.openapi.project.Project;
import git4idea.GitUtil;
import git4idea.GitVcs;
import git4idea.fetch.GitFetchResult;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.cache.PerRepoInfoCache;
import zielu.gittoolbox.compat.NotificationHandle;
import zielu.gittoolbox.compat.Notifier;
import zielu.gittoolbox.metrics.Metrics;
import zielu.gittoolbox.metrics.ProjectMetrics;
import zielu.gittoolbox.ui.util.AppUiUtil;
import zielu.gittoolbox.util.DisposeSafeCallable;
import zielu.gittoolbox.util.GtUtil;

class AutoFetchTask implements Runnable {
  private final Logger log = Logger.getInstance(getClass());
  private final AutoFetchExecutor owner;
  private final AutoFetchSchedule schedule;
  private final Project project;
  private final Supplier<List<GitRepository>> reposToFetchProvider;
  private final AutoFetchExclusions exclusions;
  private final boolean cyclic;

  private final AtomicReference<NotificationHandle> lastNotification = new AtomicReference<>();

  AutoFetchTask(@NotNull Project project, @NotNull AutoFetchExecutor owner, @NotNull AutoFetchSchedule schedule) {
    this.project = project;
    this.owner = owner;
    this.schedule = schedule;
    reposToFetchProvider = this::findAllRepos;
    exclusions = new AutoFetchExclusions(project);
    cyclic = true;
  }

  AutoFetchTask(@NotNull Project project, @NotNull AutoFetchExecutor owner, @NotNull AutoFetchSchedule schedule,
                @NotNull GitRepository repository) {
    this.project = project;
    this.owner = owner;
    this.schedule = schedule;
    reposToFetchProvider = () -> Collections.singletonList(repository);
    exclusions = new AutoFetchExclusions(project);
    cyclic = false;
  }

  private List<GitRepository> findAllRepos() {
    GitRepositoryManager repositoryManager = GitUtil.getRepositoryManager(project);
    return ImmutableList.copyOf(repositoryManager.getRepositories());
  }

  private void finishedNotification(Collection<GitRepository> fetched) {
    cancelLastNotification();
    notifyFinished(fetched);
  }

  private void cancelLastNotification() {
    Optional.ofNullable(lastNotification.getAndSet(null)).ifPresent(NotificationHandle::expire);
  }

  private void notifyFinished(Collection<GitRepository> fetched) {
    lastNotification.set(showFinishedNotification(fetched));
  }

  private NotificationHandle showFinishedNotification(Collection<GitRepository> fetched) {
    return Notifier.getInstance(project)
        .autoFetchInfo(ResBundle.message("message.autoFetch"), prepareFinishedNotificationMessage(fetched));
  }

  private String prepareFinishedNotificationMessage(Collection<GitRepository> fetched) {
    if (cyclic) {
      return ResBundle.message("message.finished");
    } else {
      return ResBundle.message("message.autoFetch.finished", reposForDisplay(fetched));
    }
  }

  private void finishedWithoutFetch() {
    cancelLastNotification();
  }

  private List<GitRepository> reposForFetch() {
    List<GitRepository> toFetch = findReposToFetch();
    log.debug("Repos to fetch: ", toFetch);
    List<GitRepository> fetchWithoutExclusions = exclusions.apply(toFetch);
    log.debug("Repos to fetch without exclusions: ", fetchWithoutExclusions);
    List<GitRepository> fetchWithRemotes = fetchWithoutExclusions.stream()
        .filter(GtUtil::hasRemotes)
        .collect(Collectors.toList());
    log.debug("Repos to fetch with remotes: ", fetchWithRemotes);
    List<GitRepository> fetchWithoutUpdatedAroundNow = schedule.filterUpdatedAroundNow(fetchWithRemotes);
    log.debug("Repos to fetch without updated around now: ", fetchWithoutUpdatedAroundNow);
    return fetchWithoutUpdatedAroundNow;
  }

  private List<GitRepository> findReposToFetch() {
    List<GitRepository> repos = reposToFetchProvider.get();
    AutoFetchStrategy strategy = AutoFetchStrategy.REPO_WITH_REMOTES;
    List<GitRepository> fetchable = strategy.fetchableRepositories(repos, project);
    return fetchable.stream().filter(this::isFetchAllowed).collect(Collectors.toList());
  }

  private boolean isFetchAllowed(@NotNull GitRepository repository) {
    return repository.getRoot().exists() && !repository.isRebaseInProgress();
  }

  private void tryToFetch(List<GitRepository> repos, @NotNull ProgressIndicator indicator, @NotNull String title) {
    log.debug("Starting auto-fetch...");
    AutoFetchState state = AutoFetchState.getInstance(project);
    if (state.canAutoFetch()) {
      log.debug("Can auto-fetch");
      doFetch(repos, indicator, title);
    } else {
      log.debug("Auto-fetch inactive");
      finishedWithoutFetch();
    }
  }

  private void doFetch(List<GitRepository> repos, @NotNull ProgressIndicator indicator, @NotNull String title) {
    AutoFetchState state = AutoFetchState.getInstance(project);
    if (state.fetchStart()) {
      indicator.setText(title);
      boolean result = tryExecuteFetch(repos, indicator);
      if (result) {
        state.fetchFinish();
      }
    } else {
      log.info("Auto-fetch already in progress");
      finishedWithoutFetch();
    }
  }

  private boolean tryExecuteFetch(List<GitRepository> repos, @NotNull ProgressIndicator indicator) {
    return owner.callIfActive(new DisposeSafeCallable<>(project, () -> {
      log.debug("Auto-fetching...");
      executeIdeaFetch(repos, indicator);
      log.debug("Finished auto-fetch");
      return true;
    }, false)).orElse(false);
  }

  private void executeIdeaFetch(@NotNull List<GitRepository> repos, @NotNull ProgressIndicator indicator) {
    Collection<GitRepository> fetched = ImmutableList.copyOf(repos);
    Metrics metrics = ProjectMetrics.getInstance(project);
    GitFetchResult fetchResult = metrics.timer("fetch-roots-idea")
        .timeSupplier(() -> GtFetchUtil.fetch(project, repos));
    fetchPerformed(fetched);
    if (fetchResult.showNotificationIfFailed(autoFetchFailedTitle())) {
      finishedNotification(fetched);
    }
    PerRepoInfoCache.getInstance(project).refresh(fetched);
  }

  private String autoFetchFailedTitle() {
    return ResBundle.message("message.autoFetch") + " " + ResBundle.message("message.failure");
  }

  private void fetchPerformed(Collection<GitRepository> fetched) {
    if (cyclic) {
      schedule.updateLastCyclicAutoFetchDate(fetched);
    } else {
      schedule.updateLastAutoFetchDate(fetched);
    }
  }

  private boolean isNotCancelled() {
    boolean cancelled = Thread.currentThread().isInterrupted();
    if (cancelled) {
      log.info("Auto-fetch task cancelled");
    }
    return !cancelled;
  }

  @Override
  public void run() {
    owner.acquireAutoFetchLock();
    if (isNotCancelled()) {
      Runnable task = () -> GitVcs.runInBackground(new Backgroundable(Preconditions.checkNotNull(project),
          ResBundle.message("message.autoFetching")) {
        @Override
        public void run(@NotNull ProgressIndicator indicator) {
          try {
            runAutoFetch(reposForFetch(), indicator);
          } finally {
            owner.releaseAutoFetchLock();
          }
        }
      });
      AppUiUtil.invokeLaterIfNeeded(project, task);
    } else {
      log.debug("Fetched skipped");
      owner.releaseAutoFetchLock();
    }
  }

  private void runAutoFetch(List<GitRepository> repos, ProgressIndicator indicator) {
    owner.runIfActive(() -> {
      if (isNotCancelled()) {
        if (!repos.isEmpty()) {
          String title = autoFetchTitle(repos);
          tryToFetch(repos, indicator, title);
        }
        if (cyclic && isNotCancelled()) {
          owner.scheduleNextTask();
        }
      }
    });
  }

  private String autoFetchTitle(List<GitRepository> repos) {
    return ResBundle.message("message.autoFetch.progress.prefix") + ": " + reposForDisplay(repos);
  }

  private String reposForDisplay(Collection<GitRepository> repos) {
    return repos.stream().map(GtUtil::name).collect(Collectors.joining(", "));
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .append("project", project)
        .toString();
  }
}
