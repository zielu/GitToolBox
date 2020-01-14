package zielu.gittoolbox.ui.statusbar

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar.Anchors
import com.intellij.openapi.wm.StatusBar.StandardWidgets
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetProvider
import zielu.gittoolbox.util.AppUtil

internal class GitStatStatusBarWidgetProvider : StatusBarWidgetProvider {
  override fun getWidget(project: Project): StatusBarWidget? {
    return if (AppUtil.hasUi()) {
      GitStatusWidget.create(project)
    } else {
      null
    }
  }

  override fun getAnchor(): String = Anchors.after(StandardWidgets.ENCODING_PANEL)
}
