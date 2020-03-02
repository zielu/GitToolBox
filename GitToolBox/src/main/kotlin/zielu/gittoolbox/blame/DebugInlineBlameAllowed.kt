package zielu.gittoolbox.blame

import com.intellij.openapi.project.Project
import com.intellij.xdebugger.XDebuggerManager
import org.slf4j.LoggerFactory
import zielu.gittoolbox.config.GitToolBoxConfig2
import zielu.gittoolbox.util.AppUtil

internal class DebugInlineBlameAllowed(private val project: Project) {

  fun isAllowed(): Boolean {
    if (GitToolBoxConfig2.getInstance().hideInlineBlameWhileDebugging) {
      val debuggerManager = XDebuggerManager.getInstance(project)
      val debugInProgress = debuggerManager.currentSession != null
      log.debug("Debug session active: ", debugInProgress)
      return !debugInProgress
    }
    return true
  }

  companion object {
    private val log = LoggerFactory.getLogger(DebugInlineBlameAllowed::class.java)

    fun getInstance(project: Project): DebugInlineBlameAllowed {
      return AppUtil.getServiceInstance(project, DebugInlineBlameAllowed::class.java)
    }
  }
}
