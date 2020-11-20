package zielu.gittoolbox.ui.statusbar

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.impl.status.widget.StatusBarWidgetsManager
import zielu.gittoolbox.config.AppConfigNotifier
import zielu.gittoolbox.config.GitToolBoxConfig2
import zielu.gittoolbox.util.AppUtil
import java.util.Optional

internal class StatusBarConfigListener(private val project: Project) : AppConfigNotifier {
  override fun configChanged(previous: GitToolBoxConfig2, current: GitToolBoxConfig2) {
    if (previous.showStatusWidget != current.showStatusWidget) {
      widgetsManager().ifPresent {
        it.updateWidget(GitStatStatusBarWidgetFactory::class.java)
      }
    }
    if (previous.showBlameWidget != current.showBlameWidget) {
      widgetsManager().ifPresent {
        it.updateWidget(GitStatStatusBarWidgetFactory::class.java)
      }
    }
  }

  private fun widgetsManager(): Optional<StatusBarWidgetsManager> {
    return AppUtil.getExistingServiceInstance(project, StatusBarWidgetsManager::class.java)
  }
}
