package zielu.gittoolbox.ui.statusBar;

import com.intellij.openapi.actionSystem.AnAction;
import git4idea.repo.GitRepository;

public interface StatusBarActions {

    static AnAction[] actionsFor(GitRepository repository) {
        return new AnAction[] {
            new FetchAction(repository)
        };
    }
}
