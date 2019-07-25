package zielu.gittoolbox.ui.statusbar.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.ui.UpdateProject;

public class UpdateAction extends DumbAwareAction {
  public UpdateAction() {
    super(ResBundle.message("update.action"));
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent event) {
    Project project = getEventProject(event);
    if (project != null) {
      UpdateProject.create(project).execute(event);
    }
  }
}
