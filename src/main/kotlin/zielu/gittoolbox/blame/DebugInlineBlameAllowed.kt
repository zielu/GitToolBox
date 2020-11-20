package zielu.gittoolbox.blame

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.settings.XDebuggerSettingsManager
import zielu.gittoolbox.util.AppUtil
import java.util.concurrent.atomic.AtomicBoolean

internal class DebugInlineBlameAllowed(val project: Project) {
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
    return if (debugInProgress.get()) {
      !debugShowsInlineValues() || !debugIsSuspended()
    } else {
      true
    }
  }

  private fun debugShowsInlineValues(): Boolean {
    return XDebuggerSettingsManager.getInstance().dataViewSettings.isShowValuesInline
  }

  private fun debugIsSuspended(): Boolean {
    return XDebuggerManager.getInstance(project).currentSession?.isSuspended ?: false
  }

  companion object {
    private val log = Logger.getInstance(DebugInlineBlameAllowed::class.java)

    fun getInstance(project: Project): DebugInlineBlameAllowed {
      return AppUtil.getServiceInstance(project, DebugInlineBlameAllowed::class.java)
    }
  }
}
