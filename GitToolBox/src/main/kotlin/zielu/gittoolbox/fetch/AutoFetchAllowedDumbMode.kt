package zielu.gittoolbox.fetch

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import org.slf4j.LoggerFactory
import zielu.gittoolbox.extension.AutoFetchAllowed
import java.util.concurrent.atomic.AtomicBoolean

internal class AutoFetchAllowedDumbMode(private val project: Project) : AutoFetchAllowed {
  private val gateway = AutoFetchAllowedLocalGateway(project)
  private val notInDumbMode = AtomicBoolean(true)
  private val initialized = AtomicBoolean()

  override fun initialize() {
    if (initialized.compareAndSet(false, true)) {
      connectMessageBus()
    }
  }

  private fun connectMessageBus() {
    project.messageBus.connect(project).subscribe(DumbService.DUMB_MODE, object : DumbService.DumbModeListener {
      override fun enteredDumbMode() {
        enterDumbMode()
      }

      override fun exitDumbMode() {
        leaveDumbMode()
      }
    })
  }

  private fun enterDumbMode() {
    log.debug("Entered dumb mode")
    notInDumbMode.set(false)
  }

  private fun leaveDumbMode() {
    log.debug("Exited dumb mode")
    notInDumbMode.set(true)
    gateway.fireStateChanged(this)
  }

  override fun isAllowed(): Boolean {
    return notInDumbMode.get()
  }

  companion object {
    private val log = LoggerFactory.getLogger(AutoFetchAllowedDumbMode::class.java)
  }
}
