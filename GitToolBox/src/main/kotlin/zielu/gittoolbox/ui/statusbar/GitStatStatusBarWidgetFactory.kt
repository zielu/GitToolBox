package zielu.gittoolbox.ui.statusbar

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBarWidget
import zielu.gittoolbox.ResBundle
import zielu.gittoolbox.config.AppConfig

internal class GitStatStatusBarWidgetFactory : GitEditorBasedWidgetFactory() {
  override fun isVisible(project: Project): Boolean {
    return AppConfig.get().showStatusWidget
  }

  override fun isEnabledByDefault(): Boolean {
    return AppConfig.get().showStatusWidget
  }

  override fun getId(): String = GitStatusWidget.ID

  override fun getDisplayName(): String = ResBundle.message("statusBar.status.displayName")

  override fun createWidget(project: Project): StatusBarWidget {
    return GitStatusWidget.create(project)
  }
}
