package zielu.gittoolbox.fetch

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import zielu.gittoolbox.util.AppUtil
import java.util.concurrent.atomic.AtomicBoolean

internal class AutoFetchAllowedDumbMode(project: Project) : Disposable {
  private val facade = AutoFetchAllowedFacade(project)
  private val inDumbMode = AtomicBoolean()

  fun enteredDumbMode() {
    log.debug("Entered dumb mode")
    inDumbMode.set(true)
  }

  fun leftDumbMode() {
    log.debug("Exited dumb mode")
    if (inDumbMode.compareAndSet(true, false)) {
      facade.fireStateChanged(this)
    }
  }

  fun isAllowed(): Boolean {
    return !inDumbMode.get()
  }

  override fun dispose() {
    // do nothing
  }

  companion object {
    private val log = Logger.getInstance(AutoFetchAllowedDumbMode::class.java)

    fun getInstance(project: Project): AutoFetchAllowedDumbMode {
      return AppUtil.getServiceInstance(project, AutoFetchAllowedDumbMode::class.java)
    }
  }
}
