package zielu.gittoolbox.ui.statusbar

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.impl.status.widget.StatusBarEditorBasedWidgetFactory
import zielu.gittoolbox.util.GtUtil

internal abstract class GitEditorBasedWidgetFactory : StatusBarEditorBasedWidgetFactory() {
  override fun disposeWidget(widget: StatusBarWidget) {
    Disposer.dispose(widget)
  }

  override fun isAvailable(project: Project): Boolean {
    return GtUtil.hasGitVcs(project) && isVisible(project)
  }

  protected abstract fun isVisible(project: Project): Boolean

  override fun isConfigurable(): Boolean {
    return false
  }
}
