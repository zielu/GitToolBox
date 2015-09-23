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
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.compat.Notifier;

public class AutoFetchTask implements Runnable {
    private final Logger LOG = Logger.getInstance(getClass());
    private final Project myProject;

    private AutoFetchTask(Project project) {
        myProject = project;
    }

    public static AutoFetchTask create(Project project) {
        return new AutoFetchTask(project);
    }

    @Override
    public void run() {
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
                        GitRepositoryManager repositoryManager = GitUtil.getRepositoryManager(myProject);
                        List<GitRepository> repos = repositoryManager.getRepositories();
                        Collection<GitRepository> fetched =
                            GtFetcher.builder().fetchAll().build(getProject(), indicator).fetchRoots(repos);
                        indicator.finishNonCancelableSection();
                        LOG.debug("Finished auto-fetch");
                        Notifier.getInstance(myProject).notifyMinorInfo(
                            ResBundle.getString("message.autoFetch"),
                            ResBundle.getString("message.finished"));
                    }
                });

            }
        });
    }
}
