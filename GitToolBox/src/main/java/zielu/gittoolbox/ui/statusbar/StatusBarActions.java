package zielu.gittoolbox.ui.statusbar;

import com.intellij.openapi.actionSystem.AnAction;
import git4idea.repo.GitRepository;
import zielu.gittoolbox.ui.statusbar.actions.FetchAction;

public interface StatusBarActions {

  static AnAction[] actionsFor(GitRepository repository) {
    return new AnAction[]{
        new FetchAction(repository)
    };
  }
}
