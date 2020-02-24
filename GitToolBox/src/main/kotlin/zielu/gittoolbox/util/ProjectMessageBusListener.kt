package zielu.gittoolbox.util

import com.intellij.openapi.project.Project
import org.slf4j.LoggerFactory

internal abstract class ProjectMessageBusListener(private val project: Project) {
  private val log = LoggerFactory.getLogger(this::class.java)

  fun handleEvent(handler: (project: Project) -> Unit) {
    val isActive = !AppUtil.runReadAction { project.isDisposedOrDisposeInProgress }
    if (isActive) {
      handler.invoke(project)
    } else {
      log.info("Event while project is disposing/disposed")
    }
  }
}
