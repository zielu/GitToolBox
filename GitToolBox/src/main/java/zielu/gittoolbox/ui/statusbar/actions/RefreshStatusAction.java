package zielu.gittoolbox.ui.statusbar.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.cache.PerRepoInfoCache;

public class RefreshStatusAction extends DumbAwareAction {

  public RefreshStatusAction() {
    super(ResBundle.getString("refresh.status.action"));
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent event) {
    Project project = getEventProject(event);
    if (project != null) {
      PerRepoInfoCache.getInstance(project).refreshAll();
    }
  }
}
