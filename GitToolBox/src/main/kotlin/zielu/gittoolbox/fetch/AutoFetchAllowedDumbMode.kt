package zielu.gittoolbox.fetch

import com.intellij.openapi.project.Project
import org.slf4j.LoggerFactory
import zielu.gittoolbox.extension.autofetch.AutoFetchAllowed
import java.util.concurrent.atomic.AtomicBoolean

internal class AutoFetchAllowedDumbMode(private val project: Project) : AutoFetchAllowed {
  private val gateway = AutoFetchAllowedLocalGateway(project)
  private val subscriber = AutoFetchAllowedDumbModeSubscriber(project)
  private val inDumbMode = AtomicBoolean()

  override fun initialize() {
    subscriber.subscribe(this::enterDumbMode, this::leaveDumbMode)
  }

  private fun enterDumbMode() {
    log.debug("Entered dumb mode")
    inDumbMode.set(true)
  }

  private fun leaveDumbMode() {
    log.debug("Exited dumb mode")
    inDumbMode.set(false)
    gateway.fireStateChanged(this)
  }

  override fun isAllowed(): Boolean {
    return !inDumbMode.get()
  }

  companion object {
    private val log = LoggerFactory.getLogger(AutoFetchAllowedDumbMode::class.java)
  }
}
