package zielu.gittoolbox.util

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project

internal abstract class ProjectMessageBusListener(private val project: Project) {
  private val log = Logger.getInstance(ProjectMessageBusListener::class.java)

  fun handleEvent(handler: (project: Project) -> Unit) {
    val isActive = !AppUtil.runReadAction { project.isDisposed }
    if (isActive) {
      handler.invoke(project)
    } else {
      log.info("Event while project is disposing/disposed")
    }
  }
}
