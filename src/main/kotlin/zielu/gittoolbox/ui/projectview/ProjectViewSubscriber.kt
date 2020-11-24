package zielu.gittoolbox.ui.projectview

import com.intellij.ide.projectView.ProjectView
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import zielu.gittoolbox.ui.util.AppUiUtil.invokeLater
import zielu.gittoolbox.util.AppUtil
import zielu.intellij.util.ZDisposeGuard

internal class ProjectViewSubscriber(
  private val project: Project
) : Disposable {
  private val disposeGuard = ZDisposeGuard()
  init {
    Disposer.register(this, disposeGuard)
  }

  fun refreshProjectView() {
    if (disposeGuard.isActive()) {
      invokeLater(disposeGuard, Runnable { refreshProjectViewInternal() })
    }
  }

  private fun refreshProjectViewInternal() {
    if (disposeGuard.isActive()) {
      ProjectView.getInstance(project).refresh()
    }
  }

  override fun dispose() {
    // do nothing
  }

  companion object {
    fun getInstance(project: Project): ProjectViewSubscriber {
      return AppUtil.getServiceInstance(project, ProjectViewSubscriber::class.java)
    }
  }
}
