package zielu.gittoolbox.blame

import com.intellij.openapi.project.Project
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XDebuggerManagerListener

internal class DebugInlineBlameAllowedListener(private val project: Project) : XDebuggerManagerListener {
  override fun currentSessionChanged(previousSession: XDebugSession?, currentSession: XDebugSession?) {
    if (currentSession != null) {
      DebugInlineBlameAllowed.getInstance(project).onDebugSessionActive()
    } else {
      DebugInlineBlameAllowed.getInstance(project).onDebugSessionInactive()
    }
  }
}
