package zielu.gittoolbox.ui.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.config.AppConfig;
import zielu.gittoolbox.config.ConfigUtil;
import zielu.gittoolbox.config.GitToolBoxConfig2;

public class InlineBlameToggleAction extends ToggleAction {

  @Override
  public boolean isSelected(@NotNull AnActionEvent e) {
    return AppConfig.get().getShowEditorInlineBlame();
  }

  @Override
  public void setSelected(@NotNull AnActionEvent e, boolean state) {
    GitToolBoxConfig2 current = AppConfig.get();
    if (current.getShowEditorInlineBlame() != state) {
      ConfigUtil.saveAppSettings(config -> config.setShowEditorInlineBlame(state));
    }
  }
}
