package zielu.gittoolbox.util

import com.intellij.openapi.project.Project
import org.slf4j.LoggerFactory

internal abstract class MessageBusListener {
  private val log = LoggerFactory.getLogger(this::class.java)

  fun handleEvent(project: Project, handler: (project: Project) -> Unit) {
    val isActive = !AppUtil.runReadAction { project.isDisposedOrDisposeInProgress }
    if (isActive) {
      handler.invoke(project)
    } else {
      log.info("Event while project is disposing/disposed")
    }
  }
}
