package zielu.gittoolbox.ui.projectview

import com.intellij.ide.projectView.ProjectView
import com.intellij.openapi.project.Project
import zielu.gittoolbox.ui.util.AppUiUtil.invokeLater
import zielu.gittoolbox.util.AppUtil

internal class ProjectViewSubscriber(private val project: Project) {
  fun refreshProjectView() {
    invokeLater(project, Runnable {
      ProjectView.getInstance(project).refresh()
    })
  }

  companion object {
    fun getInstance(project: Project): ProjectViewSubscriber {
      return AppUtil.getServiceInstance(project, ProjectViewSubscriber::class.java)
    }
  }
}
