package zielu.gittoolbox.fetch

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import org.slf4j.LoggerFactory
import zielu.gittoolbox.extension.AutoFetchAllowed
import java.util.concurrent.atomic.AtomicBoolean

internal class AutoFetchAllowedDumbMode(private val project: Project) : AutoFetchAllowed {
  private val dumbMode = AtomicBoolean()
  private val gateway = AutoFetchAllowedLocalGateway(project)

  override fun initialize() {
    project.messageBus.connect(project).subscribe(DumbService.DUMB_MODE, object : DumbService.DumbModeListener {
      override fun enteredDumbMode() {
        enterDumbMode()
      }

      override fun exitDumbMode() {
        leaveDumbMode(project)
      }
    })
  }

  private fun enterDumbMode() {
    log.debug("Entered dumb mode")
    dumbMode.set(true)
  }

  private fun leaveDumbMode(project: Project) {
    log.debug("Exited dumb mode")
    dumbMode.set(false)
    gateway.fireStateChanged(this)
  }

  override fun isAllowed(): Boolean {
    return !dumbMode.get()
  }

  companion object {
    private val log = LoggerFactory.getLogger(AutoFetchAllowedDumbMode::class.java)
  }
}
