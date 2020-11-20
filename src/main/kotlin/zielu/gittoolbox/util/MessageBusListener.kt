package zielu.gittoolbox.util

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project

internal abstract class MessageBusListener {
  private val log = Logger.getInstance(MessageBusListener::class.java)

  fun handleEvent(project: Project, handler: (project: Project) -> Unit) {
    val isActive = !AppUtil.runReadAction { project.isDisposed }
    if (isActive) {
      handler.invoke(project)
    } else {
      log.info("Event while project is disposing/disposed")
    }
  }
}
