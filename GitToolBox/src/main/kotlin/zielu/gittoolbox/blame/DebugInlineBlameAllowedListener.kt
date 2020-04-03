package zielu.gittoolbox.blame

import com.intellij.openapi.project.Project
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XDebuggerManagerListener
import zielu.gittoolbox.config.GitToolBoxConfig2

internal class DebugInlineBlameAllowedListener(private val project: Project) : XDebuggerManagerListener {
  override fun currentSessionChanged(previousSession: XDebugSession?, currentSession: XDebugSession?) {
    if (GitToolBoxConfig2.getInstance().alwaysShowInlineBlameWhileDebugging) {
      if (currentSession != null) {
        DebugInlineBlameAllowed.getInstance(project).onDebugSessionActive()
      } else {
        DebugInlineBlameAllowed.getInstance(project).onDebugSessionInactive()
      }
    }
  }
}
