package zielu.gittoolbox.ui.statusbar

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBarWidget
import zielu.gittoolbox.ResBundle
import zielu.gittoolbox.config.AppConfig

internal class BlameStatusBarWidgetFactory : GitEditorBasedWidgetFactory() {
  override fun isVisible(project: Project): Boolean {
    return AppConfig.getConfig().showBlameWidget
  }

  override fun isEnabledByDefault(): Boolean {
    return AppConfig.getConfig().showBlameWidget
  }

  override fun getId(): String = BlameStatusWidget.ID

  override fun getDisplayName(): String = ResBundle.message("statusBar.blame.displayName")

  override fun createWidget(project: Project): StatusBarWidget {
    return BlameStatusWidget(project)
  }
}
