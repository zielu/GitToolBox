package zielu.gittoolbox.ui.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.config.ConfigUtil;
import zielu.gittoolbox.config.GitToolBoxConfig2;

public class InlineBlameToggleAction extends ToggleAction {

  @Override
  public boolean isSelected(@NotNull AnActionEvent e) {
    return GitToolBoxConfig2.getInstance().showEditorInlineBlame;
  }

  @Override
  public void setSelected(@NotNull AnActionEvent e, boolean state) {
    GitToolBoxConfig2 current = GitToolBoxConfig2.getInstance();
    if (current.isShowEditorInlineBlameChanged(state)) {
      ConfigUtil.saveAppSettings(config -> config.showEditorInlineBlame = state);
    }
  }
}
