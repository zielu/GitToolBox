package zielu.gittoolbox.actions;

import com.google.common.base.Preconditions;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task.Backgroundable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.GitUtil;
import git4idea.GitVcs;
import git4idea.actions.GitRepositoryAction;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import git4idea.util.GitUIUtil;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.status.GitAheadBehindStatus;
import zielu.gittoolbox.status.GitStatusCalculator;

public class GitShowStatusAction extends GitRepositoryAction {
    @NotNull
    @Override
    protected String getActionName() {
        return "Show status";
    }

    @Override
    protected void perform(@NotNull Project project, @NotNull final List<VirtualFile> gitRoots, 
                           @NotNull VirtualFile defaultRoot, final Set<VirtualFile> affectedRoots, 
                           List<VcsException> exceptions) throws VcsException {
        GitVcs.runInBackground(new Backgroundable(Preconditions.checkNotNull(project), "Git show status", false) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                GitRepositoryManager repositoryManager = GitUtil.getRepositoryManager(getProject());
                Collection<GitRepository> repositories = GitUtil.getRepositoriesFromRoots(repositoryManager, 
                    Preconditions.checkNotNull(gitRoots));
                GitStatusCalculator calc = GitStatusCalculator.create(getProject(), progressIndicator);
                List<GitAheadBehindStatus> statuses = calc.aheadBehindStatus(repositories);
                if (!statuses.isEmpty()) {
                    GitUIUtil.notifySuccess(myProject, "", prepareMessage(repositories, statuses));
                }
            }
        });
    }
    
    private String prepareMessage(Collection<GitRepository> repositories, List<GitAheadBehindStatus> statuses) {
        StringBuilder message = new StringBuilder("Status:");
        if (statuses.size() == 1) {
            message.append(" ").append(statuses.get(0));    
        } else {
            int index = 0;
            for (GitRepository repository : repositories) {
                message.append("\n").append(repository.getGitDir().getName()).append(statuses.get(index));
                index++;
            }
        }
        return message.toString();
    }
}
