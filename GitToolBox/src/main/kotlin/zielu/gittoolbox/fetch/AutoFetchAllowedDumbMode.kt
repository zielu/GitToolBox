package zielu.gittoolbox.fetch

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import zielu.gittoolbox.util.AppUtil
import java.util.concurrent.atomic.AtomicBoolean

internal class AutoFetchAllowedDumbMode(project: Project) {
  private val gateway = AutoFetchAllowedLocalGateway(project)
  private val inDumbMode = AtomicBoolean()

  fun enteredDumbMode() {
    log.debug("Entered dumb mode")
    inDumbMode.set(true)
  }

  fun leftDumbMode() {
    log.debug("Exited dumb mode")
    if (inDumbMode.compareAndSet(true, false)) {
      gateway.fireStateChanged()
    }
  }

  fun isAllowed(): Boolean {
    return !inDumbMode.get()
  }

  companion object {
    private val log = Logger.getInstance(AutoFetchAllowedDumbMode::class.java)

    fun getInstance(project: Project): AutoFetchAllowedDumbMode {
      return AppUtil.getServiceInstance(project, AutoFetchAllowedDumbMode::class.java)
    }
  }
}
