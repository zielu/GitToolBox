package zielu.gittoolbox.ui.statusBar;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.util.GtUtil;

public class RepositoryActions extends ActionGroup {
    private final GitRepository myRepository;

    public RepositoryActions(GitRepository repository) {
        super(GtUtil.name(repository), true);
        myRepository = repository;
    }

    @NotNull
    @Override
    public AnAction[] getChildren(@Nullable AnActionEvent e) {
        return new AnAction[] {
            new FetchAction(myRepository)
        };
    }
}
