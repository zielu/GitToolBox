package zielu.gittoolbox.util

import com.intellij.openapi.project.Project

internal abstract class MessageBusListener {
  fun handleEvent(project: Project, handler: (project: Project) -> Unit) {
    val isActive = !AppUtil.runReadAction { project.isDisposedOrDisposeInProgress }
    if (isActive) {
      handler.invoke(project)
    }
  }
}
