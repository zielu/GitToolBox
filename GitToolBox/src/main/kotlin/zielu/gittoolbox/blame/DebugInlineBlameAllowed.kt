package zielu.gittoolbox.blame

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.xdebugger.XDebuggerManager
import zielu.gittoolbox.util.AppUtil
import java.util.concurrent.atomic.AtomicBoolean

internal class DebugInlineBlameAllowed(project: Project) {
  private val debugInProgress = AtomicBoolean()

  init {
    debugInProgress.set(XDebuggerManager.getInstance(project).currentSession != null)
    log.debug("Debug session active: ", debugInProgress.get())
  }

  fun onDebugSessionActive() {
    if (debugInProgress.compareAndSet(false, true)) {
      log.debug("Debug session actived")
    } else {
      log.debug("Debug session is already active")
    }
  }

  fun onDebugSessionInactive() {
    if (debugInProgress.compareAndSet(true, false)) {
      log.debug("Debug session deactivated")
    } else {
      log.debug("Debug session is already inactive")
    }
  }

  fun isAllowed(): Boolean {
    return !debugInProgress.get()
  }

  companion object {
    private val log = Logger.getInstance(DebugInlineBlameAllowed::class.java)

    fun getInstance(project: Project): DebugInlineBlameAllowed {
      return AppUtil.getServiceInstance(project, DebugInlineBlameAllowed::class.java)
    }
  }
}
