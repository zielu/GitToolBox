package zielu.gittoolbox.ui.statusBar.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
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
    public void actionPerformed(AnActionEvent e) {
        GtFetchUtil.fetch(myRepository);
    }
}
