package zielu.gittoolbox.fetch;

import com.google.common.base.Preconditions;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task.Backgroundable;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.UIUtil;
import git4idea.GitUtil;
import git4idea.GitVcs;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.compat.NotificationHandle;
import zielu.gittoolbox.compat.Notifier;
import zielu.gittoolbox.util.GtUtil;

public class AutoFetchTask implements Runnable {
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
        NotificationHandle toCancel = lastNotification.get();
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
        NotificationHandle toCancel = lastNotification.get();
        if (toCancel != null && myParent.isActive()) {
            toCancel.expire();
        }
        lastNotification.set(null);
    }

    private boolean shouldFetch(List<GitRepository> repos) {
        boolean fetch = false;
        for (GitRepository repo : repos) {
            if (GtUtil.isFetchable(repo)) {
                fetch = true;
                break;
            }
        }
        return fetch;
    }

    @Override
    public void run() {
        GitRepositoryManager repositoryManager = GitUtil.getRepositoryManager(myProject);
        final List<GitRepository> repos = repositoryManager.getRepositories();
        if (shouldFetch(repos) && myParent.isActive()) {
            UIUtil.invokeLaterIfNeeded(new Runnable() {
                @Override
                public void run() {
                    GitVcs.runInBackground(new Backgroundable(Preconditions.checkNotNull(myProject),
                        ResBundle.getString("message.autoFetching"), false) {
                        @Override
                        public void run(@NotNull ProgressIndicator indicator) {
                            LOG.debug("Starting auto-fetch...");
                            indicator.setText(getTitle());
                            indicator.setIndeterminate(true);
                            indicator.startNonCancelableSection();
                            if (myParent.isActive()) {
                                Collection<GitRepository> fetched =
                                    GtFetcher.builder().fetchAll().build(getProject(), indicator).fetchRoots(repos);
                                indicator.finishNonCancelableSection();
                                LOG.debug("Finished auto-fetch");
                                finishedNotification();
                            }
                        }
                    });
                }
            });
        } else {
            UIUtil.invokeLaterIfNeeded(new Runnable() {
                @Override
                public void run() {
                    finishedWithoutFetch();
                }
            });
        }
    }
}
