package zielu.gittoolbox.ui.statusbar

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBarWidget
import zielu.gittoolbox.config.GitToolBoxConfig2

internal class BlameStatusBarWidgetFactory : GitEditorBasedWidgetFactory() {
  override fun isVisible(project: Project): Boolean {
    return GitToolBoxConfig2.getInstance().showBlameWidget
  }

  override fun isEnabledByDefault(): Boolean {
    return GitToolBoxConfig2.getInstance().showBlameWidget
  }

  override fun getId(): String = BlameStatusWidget.ID

  override fun getDisplayName(): String = "GitToolBox: Blame"

  override fun createWidget(project: Project): StatusBarWidget {
    return BlameStatusWidget(project)
  }
}
