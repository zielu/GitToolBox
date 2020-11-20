package zielu.gittoolbox.ui.statusbar;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.util.GtUtil;

public class RepositoryActions extends ActionGroup {
  private final GitRepository repository;

  public RepositoryActions(GitRepository repository) {
    super(GtUtil.name(repository), true);
    this.repository = repository;
  }

  @NotNull
  @Override
  public AnAction[] getChildren(@Nullable AnActionEvent e) {
    return StatusBarActions.actionsFor(repository);
  }
}
