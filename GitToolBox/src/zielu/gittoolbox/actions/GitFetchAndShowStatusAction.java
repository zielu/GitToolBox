package zielu.gittoolbox.actions;

import com.google.common.base.Preconditions;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task.Backgroundable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.VcsNotifier;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.GitUtil;
import git4idea.GitVcs;
import git4idea.actions.GitRepositoryAction;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import git4idea.update.GitFetcher;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.status.GitStatusCalculator;
import zielu.gittoolbox.status.StatusMessages;

public class GitFetchAndShowStatusAction extends GitRepositoryAction {
    @NotNull
    @Override
    protected String getActionName() {
        return "Fetch and show status";
    }

    @Override
    protected void perform(@NotNull Project project, @NotNull final List<VirtualFile> gitRoots,
                           @NotNull VirtualFile defaultRoot, final Set<VirtualFile> affectedRoots,
                           List<VcsException> exceptions) throws VcsException {
        GitVcs.runInBackground(new Backgroundable(Preconditions.checkNotNull(project), "Fetching...", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                GitRepositoryManager repositoryManager = GitUtil.getRepositoryManager(getProject());
                Collection<GitRepository> repositories = GitUtil.getRepositoriesFromRoots(repositoryManager,
                    Preconditions.checkNotNull(gitRoots));
                new GitFetcher(getProject(), indicator, true)
                    .fetchRootsAndNotify(GitUtil.getRepositoriesFromRoots(repositoryManager, gitRoots), null, false);
                GitStatusCalculator calc = GitStatusCalculator.create(getProject(), indicator);
                List<Integer> statuses = calc.behindStatus(repositories);
                if (!statuses.isEmpty()) {
                    VcsNotifier.getInstance(getProject()).notifySuccess(StatusMessages.prepareBehindMessage(repositories, statuses));
                }
            }
        });
    }

}
