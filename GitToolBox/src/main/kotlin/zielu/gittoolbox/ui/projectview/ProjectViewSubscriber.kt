package zielu.gittoolbox.ui.projectview

import com.intellij.ide.projectView.ProjectView
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import zielu.gittoolbox.ui.util.AppUiUtil.invokeLater
import zielu.gittoolbox.util.AppUtil
import java.util.concurrent.atomic.AtomicBoolean

internal class ProjectViewSubscriber(
  private val project: Project
) : Disposable {
  private val active = AtomicBoolean(true)

  fun refreshProjectView() {
    if (active.get()) {
      invokeLater(project, Runnable {
        if (active.get()) {
          ProjectView.getInstance(project).refresh()
        }
      })
    }
  }

  override fun dispose() {
    active.compareAndSet(true, false)
  }

  companion object {
    fun getInstance(project: Project): ProjectViewSubscriber {
      return AppUtil.getServiceInstance(project, ProjectViewSubscriber::class.java)
    }
  }
}
