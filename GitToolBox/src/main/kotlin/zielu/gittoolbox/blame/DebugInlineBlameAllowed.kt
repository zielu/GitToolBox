package zielu.gittoolbox.blame

import com.intellij.openapi.project.Project
import com.intellij.xdebugger.XDebuggerManager
import org.slf4j.LoggerFactory
import zielu.gittoolbox.util.AppUtil

internal class DebugInlineBlameAllowed(private val project: Project) {

  fun isAllowed(): Boolean {
    val debuggerManager = XDebuggerManager.getInstance(project)
    val debugInProgress = debuggerManager.currentSession != null
    log.debug("Debug session in progress: ", debugInProgress)
    return !debugInProgress
  }

  companion object {
    private val log = LoggerFactory.getLogger(DebugInlineBlameAllowed::class.java)

    fun getInstance(project: Project): DebugInlineBlameAllowed {
      return AppUtil.getServiceInstance(project, DebugInlineBlameAllowed::class.java)
    }
  }
}
