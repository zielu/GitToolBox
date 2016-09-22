package zielu.gittoolbox.ui.statusBar;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import git4idea.GitLocalBranch;
import git4idea.GitRemoteBranch;
import git4idea.i18n.GitBundle;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.fetch.GtFetchUtil;

public class FetchAction extends DumbAwareAction {
    private final GitRepository myRepository;

    public FetchAction(@NotNull GitRepository repository) {
        super(GitBundle.getString("fetch.action.name"));
        myRepository = repository;
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(false);
        GitLocalBranch branch = myRepository.getCurrentBranch();
        if (branch != null) {
            GitRemoteBranch trackedBranch = branch.findTrackedBranch(myRepository);
            if (trackedBranch != null) {
                presentation.setText(GitBundle.getString("fetch.action.name")+" "+trackedBranch.getNameForLocalOperations());
                presentation.setEnabled(true);
            }
        }
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        GtFetchUtil.fetch(myRepository);
    }
}
