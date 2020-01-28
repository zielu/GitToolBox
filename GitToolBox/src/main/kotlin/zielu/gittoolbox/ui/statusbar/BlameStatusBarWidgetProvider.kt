package zielu.gittoolbox.ui.statusbar

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar.Anchors
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetProvider
import zielu.gittoolbox.util.AppUtil

internal class BlameStatusBarWidgetProvider : StatusBarWidgetProvider {
  override fun getWidget(project: Project): StatusBarWidget? {
    return if (AppUtil.hasUi()) {
      BlameStatusWidget(project)
    } else {
      null
    }
  }

  override fun getAnchor(): String = Anchors.after(GitStatusWidget.ID)
}
