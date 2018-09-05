package zielu.gittoolbox.fetch;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task.Backgroundable;
import com.intellij.openapi.project.Project;
import git4idea.GitUtil;
import git4idea.GitVcs;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.cache.PerRepoInfoCache;
import zielu.gittoolbox.compat.NotificationHandle;
import zielu.gittoolbox.compat.Notifier;
import zielu.gittoolbox.config.GitToolBoxConfigForProject;
import zielu.gittoolbox.ui.util.AppUtil;
import zielu.gittoolbox.util.DisposeSafeCallable;
import zielu.gittoolbox.util.GtUtil;

class AutoFetchTask implements Runnable {
  private static final boolean showNotifications = false;

  private final Logger log = Logger.getInstance(getClass());
  private final AutoFetchExecutor owner;
  private final Project project;

  private final AtomicReference<NotificationHandle> lastNotification = new AtomicReference<NotificationHandle>();

  AutoFetchTask(@NotNull Project project, AutoFetchExecutor owner) {
    this.project = project;
    this.owner = owner;
  }

  private void finishedNotification() {
    cancelLastNotification();
    notifyFinished();
  }

  private void cancelLastNotification() {
    Optional.ofNullable(lastNotification.getAndSet(null)).ifPresent(NotificationHandle::expire);
  }

  private void notifyFinished() {
    lastNotification.set(showFinishedNotification());
  }

  private NotificationHandle showFinishedNotification() {
    return Notifier.getInstance(project).autoFetchInfo(
        ResBundle.getString("message.autoFetch"),
        ResBundle.getString("message.finished"));
  }

  private void finishedWithoutFetch() {
    cancelLastNotification();
  }

  private List<GitRepository> reposForFetch() {
    List<GitRepository> toFetch = findReposToFetch();
    log.debug("Repos to fetch: ", toFetch);
    List<GitRepository> fetchWithoutExclusions = skipExclusions(toFetch);
    log.debug("Repos to fetch without exclusions: ", fetchWithoutExclusions);
    return fetchWithoutExclusions;
  }

  private List<GitRepository> findReposToFetch() {
    GitRepositoryManager repositoryManager = GitUtil.getRepositoryManager(project);
    ImmutableList<GitRepository> allRepos = ImmutableList.copyOf(repositoryManager.getRepositories());
    AutoFetchStrategy strategy = GitToolBoxConfigForProject.getInstance(project).getAutoFetchStrategy();
    List<GitRepository> fetchable = strategy.fetchableRepositories(allRepos, project);
    return fetchable.stream().filter(this::isFetchAllowed).collect(Collectors.toList());
  }

  private boolean isFetchAllowed(@NotNull GitRepository repository) {
    return repository.getRoot().exists() && !repository.isRebaseInProgress();
  }

  private List<GitRepository> skipExclusions(List<GitRepository> repositories) {
    GitToolBoxConfigForProject config = GitToolBoxConfigForProject.getInstance(project);
    Set<String> exclusions = new HashSet<>(config.autoFetchExclusions);
    return repositories.stream().filter(rootExcluded(exclusions).negate()).collect(Collectors.toList());
  }

  private Predicate<GitRepository> rootExcluded(Set<String> exclusions) {
    return repo -> exclusions.contains(repo.getRoot().getUrl());
  }

  private boolean tryToFetch(List<GitRepository> repos, @NotNull ProgressIndicator indicator, @NotNull String title) {
    log.debug("Starting auto-fetch...");
    boolean result = false;
    AutoFetchState state = AutoFetchState.getInstance(project);
    if (state.canAutoFetch()) {
      log.debug("Can auto-fetch");
      result = doFetch(repos, indicator, title);
    } else {
      log.debug("Auto-fetch inactive");
      finishedWithoutFetch();
    }
    return result;
  }

  private boolean doFetch(List<GitRepository> repos, @NotNull ProgressIndicator indicator, @NotNull String title) {
    boolean result;
    AutoFetchState state = AutoFetchState.getInstance(project);
    if (state.fetchStart()) {
      indicator.setText(title);
      result = tryExecuteFetch(repos, indicator);
      if (result) {
        state.fetchFinish();
        fetchSuccessful();
      }
    } else {
      log.info("Auto-fetch already in progress");
      finishedWithoutFetch();
      result = true;
    }
    return result;
  }

  private boolean tryExecuteFetch(List<GitRepository> repos, @NotNull ProgressIndicator indicator) {
    return owner.callIfActive(new DisposeSafeCallable<>(project, () -> {
      log.debug("Auto-fetching...");
      executeFetch(repos, indicator);
      log.debug("Finished auto-fetch");
      if (showNotifications) {
        finishedNotification();
      }
      return true;
    }, false)).orElse(false);
  }

  private void executeFetch(List<GitRepository> repos, @NotNull ProgressIndicator indicator) {
    Collection<GitRepository> fetched = GtFetcher.builder().fetchAll().withExecutor(owner.repoFetchExecutor())
        .build(project, indicator).fetchRoots(repos);
    PerRepoInfoCache.getInstance(project).refresh(fetched);
  }

  private void fetchSuccessful() {
    owner.updateLastAutoFetchDate();
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
    final List<GitRepository> repos = reposForFetch();
    boolean shouldFetch = !repos.isEmpty();
    if (shouldFetch && isNotCancelled()) {
      AppUtil.invokeLaterIfNeeded(() -> GitVcs.runInBackground(new Backgroundable(Preconditions.checkNotNull(project),
          ResBundle.getString("message.autoFetching")) {
        @Override
        public void run(@NotNull ProgressIndicator indicator) {
          runAutoFetch(repos, indicator);
        }
      }));
    } else {
      log.debug("Fetched skipped");
      if (showNotifications) {
        AppUtil.invokeLaterIfNeeded(this::finishedWithoutFetch);
      }
    }
  }

  private void runAutoFetch(List<GitRepository> repos, ProgressIndicator indicator) {
    owner.runIfActive(() -> {
      if (isNotCancelled()) {
        String title = autoFetchTitle(repos);
        if (tryToFetch(repos, indicator, title) && isNotCancelled()) {
          owner.scheduleNextTask();
        }
      }
    });
  }

  private String autoFetchTitle(List<GitRepository> repos) {
    String reposPart = repos.stream().map(GtUtil::name).collect(Collectors.joining(", "));
    return ResBundle.getString("message.autoFetch.progress.prefix") + ": " + reposPart;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .append("project", project)
        .toString();
  }
}
