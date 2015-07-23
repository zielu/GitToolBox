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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.compat.Notifier;
import zielu.gittoolbox.fetch.GtFetcher;
import zielu.gittoolbox.status.GitStatusCalculator;
import zielu.gittoolbox.status.RevListCount;
import zielu.gittoolbox.ui.StatusMessages;

public class GitFetchAndShowStatusAction extends GitRepositoryAction {
    @NotNull
    @Override
    protected String getActionName() {
        return ResBundle.getString("action.fetch.and.show.status");
    }

    @Override
    protected void perform(@NotNull final Project project, @NotNull final List<VirtualFile> gitRoots,
                           @NotNull VirtualFile defaultRoot, final Set<VirtualFile> affectedRoots,
                           List<VcsException> exceptions) throws VcsException {
        GitVcs.runInBackground(new Backgroundable(Preconditions.checkNotNull(project), ResBundle.getString("message.fetching"), false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                GitRepositoryManager repositoryManager = GitUtil.getRepositoryManager(getProject());
                Collection<GitRepository> repositories = GitUtil.getRepositoriesFromRoots(repositoryManager,
                    Preconditions.checkNotNull(gitRoots));

                Collection<GitRepository> fetched =
                    GtFetcher.builder().fetchAll().build(getProject(), indicator).fetchRoots(repositories);
                GitStatusCalculator calc = GitStatusCalculator.create(getProject(), indicator);
                Map<GitRepository, RevListCount> statuses = calc.behindStatus(fetched);
                if (!statuses.isEmpty()) {
                    Notifier.getInstance(getProject()).notifySuccess(StatusMessages.getInstance().prepareBehindMessage(statuses));
                }
            }
        });
    }

}
