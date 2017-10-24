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
import zielu.gittoolbox.GitToolBoxConfigForProject;
import zielu.gittoolbox.GitToolBoxProject;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.compat.NotificationHandle;
import zielu.gittoolbox.compat.Notifier;
import zielu.gittoolbox.ui.util.AppUtil;

public class AutoFetchTask implements Runnable {
    private static final boolean showNotifications = false;

    private final Logger LOG = Logger.getInstance(getClass());
    private final AutoFetch myParent;
    private final Project myProject;

    private final AtomicReference<NotificationHandle> lastNotification = new AtomicReference<NotificationHandle>();

    private AutoFetchTask(AutoFetch parent) {
        myParent = parent;
        myProject = myParent.project();
    }

    public static AutoFetchTask create(AutoFetch parent) {
        return new AutoFetchTask(parent);
    }

    private void finishedNotification() {
        NotificationHandle toCancel = lastNotification.getAndSet(null);
        if (toCancel != null) {
            toCancel.expire();
        }

        NotificationHandle notification = Notifier.getInstance(myProject).notifyLogOnly(
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
        GitRepositoryManager repositoryManager = GitUtil.getRepositoryManager(myProject);
        ImmutableList<GitRepository> allRepos = ImmutableList.copyOf(repositoryManager.getRepositories());
        AutoFetchStrategy strategy = GitToolBoxConfigForProject.getInstance(myProject).getAutoFetchStrategy();
        List<GitRepository> fetchable = strategy.fetchableRepositories(allRepos, myProject);
        List<GitRepository> toFetch = Lists.newArrayListWithCapacity(fetchable.size());
        for (GitRepository repository : fetchable) {
            if (repository.getRoot().exists() && !repository.isRebaseInProgress()) {
                toFetch.add(repository);
            }
        }
        return toFetch;
    }

    private boolean doFetch(List<GitRepository> repos, @NotNull ProgressIndicator indicator, @NotNull String title) {
        LOG.debug("Starting auto-fetch...");
        boolean result = false;
        AutoFetchState state = AutoFetchState.getInstance(myProject);
        if (state.canAutoFetch()) {
            LOG.debug("Can auto-fetch");
            if (state.fetchStart()) {
                indicator.setText(title);
                indicator.setIndeterminate(true);
                indicator.startNonCancelableSection();
                result = doFetch(repos, indicator);
                if (result) {
                    indicator.finishNonCancelableSection();
                    state.fetchFinish();
                    myParent.updateLastAutoFetchDate();
                }
            } else {
                LOG.info("Auto-fetch already in progress");
                finishedWithoutFetch();
                result = true;
            }
        } else {
            LOG.debug("Auto-fetch inactive");
            finishedWithoutFetch();
        }
        return result;
    }

    private boolean doFetch(List<GitRepository> repos, @NotNull ProgressIndicator indicator) {
        return myParent.callIfActive(() -> {
            LOG.debug("Auto-fetching...");
            try {
                Collection<GitRepository> fetched = GtFetcher.builder().fetchAll().build(myProject, indicator).fetchRoots(repos);
                GitToolBoxProject.getInstance(myProject).perRepoStatusCache().refresh(fetched);
                LOG.debug("Finished auto-fetch");
                if (showNotifications) {
                    finishedNotification();
                }
            } catch (AssertionError error) {
                if (myProject.isDisposed()) {
                    LOG.debug("Project already disposed", error);
                } else {
                    LOG.error(error);
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
            AppUtil.invokeLaterIfNeeded(() -> GitVcs.runInBackground(new Backgroundable(Preconditions.checkNotNull(myProject),
                ResBundle.getString("message.autoFetching"), false) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    myParent.runIfActive(() -> {
                        if (doFetch(repos, indicator, getTitle())) {
                            myParent.scheduleNextTask();
                        }
                    });
                }
            }));
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Fetched skipped");
            }
            if (showNotifications) {
                AppUtil.invokeLaterIfNeeded(this::finishedWithoutFetch);
            }
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("project", myProject)
            .toString();
    }
}
