package zielu.gittoolbox.fetch;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task.Backgroundable;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import git4idea.GitUtil;
import git4idea.GitVcs;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
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
        if (toCancel != null && myParent.isActive()) {
            toCancel.expire();
        }
        if (myParent.isActive()) {
            NotificationHandle notification = Notifier.getInstance(myProject).notifyLogOnly(
                ResBundle.getString("message.autoFetch"),
                ResBundle.getString("message.finished"));
            lastNotification.set(notification);
        }
    }

    private void finishedWithoutFetch() {
        NotificationHandle toCancel = lastNotification.getAndSet(null);
        if (toCancel != null && myParent.isActive()) {
            toCancel.expire();
        }
    }

    private boolean isSmartMode() {
        return !DumbService.isDumb(myProject);
    }

    private boolean isEnabled() {
        return myParent.isActive() && isSmartMode();
    }

    private List<GitRepository> reposForFetch() {
        GitRepositoryManager repositoryManager = GitUtil.getRepositoryManager(myProject);
        ImmutableList<GitRepository> allRepos = ImmutableList.copyOf(repositoryManager.getRepositories());
        AutoFetchStrategy strategy = GitToolBoxConfigForProject.getInstance(myProject).getAutoFetchStrategy();
        List<GitRepository> fetchable = strategy.fetchableRepositories(allRepos, myProject);
        List<GitRepository> toFetch = Lists.newArrayListWithCapacity(fetchable.size());
        for (GitRepository repository : fetchable) {
            if (repository.getRoot().exists()) {
                toFetch.add(repository);
            }
        }
        return toFetch;
    }

    private void doFetch(List<GitRepository> repos, @NotNull ProgressIndicator indicator, @NotNull String title) {
        LOG.debug("Starting auto-fetch...");
        indicator.setText(title);
        indicator.setIndeterminate(true);
        indicator.startNonCancelableSection();
        if (isEnabled()) {
            Collection<GitRepository> fetched =
                GtFetcher.builder().fetchAll().build(myProject, indicator).fetchRoots(repos);
            indicator.finishNonCancelableSection();
            GitToolBoxProject.getInstance(myProject).perRepoStatusCache().refresh(fetched);
            LOG.debug("Finished auto-fetch");
            if (showNotifications) {
                finishedNotification();
            }
        } else {
            LOG.debug("Skipped auto-fetch");
        }
        myParent.updateLastAutoFetchDate();
    }


    @Override
    public void run() {
        final List<GitRepository> repos = reposForFetch();
        boolean shouldFetch = !repos.isEmpty();
        boolean enabled = isEnabled();
        if (shouldFetch && enabled) {
            AppUtil.invokeLaterIfNeeded(() -> GitVcs.runInBackground(new Backgroundable(Preconditions.checkNotNull(myProject),
                ResBundle.getString("message.autoFetching"), false) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    doFetch(repos, indicator, getTitle());
                }
            }));
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Fetched skipped: shouldFetch=" + shouldFetch + ", enabled=" + enabled);
            }
            if (showNotifications) {
                AppUtil.invokeLaterIfNeeded(this::finishedWithoutFetch);
            }
        }
    }
}
