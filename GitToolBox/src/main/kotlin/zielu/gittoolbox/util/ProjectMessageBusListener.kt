package zielu.gittoolbox.util

import com.intellij.openapi.project.Project

internal abstract class ProjectMessageBusListener(private val project: Project) {
  fun handleEvent(handler: (project: Project) -> Unit) {
    val isActive = !AppUtil.runReadAction { project.isDisposedOrDisposeInProgress }
    if (isActive) {
      handler.invoke(project)
    }
  }
}
