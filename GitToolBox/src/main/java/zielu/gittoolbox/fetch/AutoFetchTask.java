package zielu.gittoolbox.fetch;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task.Backgroundable;
import com.intellij.openapi.project.Project;
import git4idea.GitUtil;
import git4idea.GitVcs;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.GitToolBoxProject;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.compat.NotificationHandle;
import zielu.gittoolbox.compat.Notifier;
import zielu.gittoolbox.config.GitToolBoxConfigForProject;
import zielu.gittoolbox.ui.util.AppUtil;

public class AutoFetchTask implements Runnable {
  private static final boolean showNotifications = false;

  private final Logger log = Logger.getInstance(getClass());
  private final AutoFetch parent;
  private final Project project;

  private final AtomicReference<NotificationHandle> lastNotification = new AtomicReference<NotificationHandle>();

  private AutoFetchTask(AutoFetch parent) {
    this.parent = parent;
    project = this.parent.project();
  }

  public static AutoFetchTask create(AutoFetch parent) {
    return new AutoFetchTask(parent);
  }

  private void finishedNotification() {
    NotificationHandle toCancel = lastNotification.getAndSet(null);
    if (toCancel != null) {
      toCancel.expire();
    }

    NotificationHandle notification = Notifier.getInstance(project).notifyLogOnly(
        ResBundle.getString("message.autoFetch"),
        ResBundle.getString("message.finished"));
    lastNotification.set(notification);
  }

  private void finishedWithoutFetch() {
    NotificationHandle toCancel = lastNotification.getAndSet(null);
    if (toCancel != null) {
      toCancel.expire();
    }
  }

  private List<GitRepository> reposForFetch() {
    GitRepositoryManager repositoryManager = GitUtil.getRepositoryManager(project);
    ImmutableList<GitRepository> allRepos = ImmutableList.copyOf(repositoryManager.getRepositories());
    AutoFetchStrategy strategy = GitToolBoxConfigForProject.getInstance(project).getAutoFetchStrategy();
    List<GitRepository> fetchable = strategy.fetchableRepositories(allRepos, project);
    List<GitRepository> toFetch = Lists.newArrayListWithCapacity(fetchable.size());
    for (GitRepository repository : fetchable) {
      if (repository.getRoot().exists() && !repository.isRebaseInProgress()) {
        toFetch.add(repository);
      }
    }
    return toFetch;
  }

  private boolean doFetch(List<GitRepository> repos, @NotNull ProgressIndicator indicator, @NotNull String title) {
    log.debug("Starting auto-fetch...");
    boolean result = false;
    AutoFetchState state = AutoFetchState.getInstance(project);
    if (state.canAutoFetch()) {
      log.debug("Can auto-fetch");
      if (state.fetchStart()) {
        indicator.setText(title);
        indicator.setIndeterminate(true);
        indicator.startNonCancelableSection();
        result = doFetch(repos, indicator);
        if (result) {
          indicator.finishNonCancelableSection();
          state.fetchFinish();
          parent.updateLastAutoFetchDate();
        }
      } else {
        log.info("Auto-fetch already in progress");
        finishedWithoutFetch();
        result = true;
      }
    } else {
      log.debug("Auto-fetch inactive");
      finishedWithoutFetch();
    }
    return result;
  }

  private boolean doFetch(List<GitRepository> repos, @NotNull ProgressIndicator indicator) {
    return parent.callIfActive(() -> {
      log.debug("Auto-fetching...");
      try {
        Collection<GitRepository> fetched = GtFetcher.builder().fetchAll().build(project, indicator).fetchRoots(repos);
        GitToolBoxProject.getInstance(project).perRepoStatusCache().refresh(fetched);
        log.debug("Finished auto-fetch");
        if (showNotifications) {
          finishedNotification();
        }
      } catch (AssertionError error) {
        if (project.isDisposed()) {
          log.debug("Project already disposed", error);
        } else {
          log.error(error);
          return false;
        }
      }
      return true;
    }).orElse(false);
  }

  @Override
  public void run() {
    final List<GitRepository> repos = reposForFetch();
    boolean shouldFetch = !repos.isEmpty();
    if (shouldFetch) {
      AppUtil.invokeLaterIfNeeded(() -> GitVcs.runInBackground(new Backgroundable(Preconditions.checkNotNull(project),
          ResBundle.getString("message.autoFetching"), false) {
        @Override
        public void run(@NotNull ProgressIndicator indicator) {
          parent.runIfActive(() -> {
            if (doFetch(repos, indicator, getTitle())) {
              parent.scheduleNextTask();
            }
          });
        }
      }));
    } else {
      log.debug("Fetched skipped");
      if (showNotifications) {
        AppUtil.invokeLaterIfNeeded(this::finishedWithoutFetch);
      }
    }
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .append("project", project)
        .toString();
  }
}
