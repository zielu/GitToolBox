package zielu.gittoolbox.ui.actions.diagnostic

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import zielu.gittoolbox.GitToolBoxRegistry

internal class DiagnosticGroup : ActionGroup() {
  override fun getChildren(e: AnActionEvent?): Array<AnAction> {
    return if (GitToolBoxRegistry.diagnosticMode())
      arrayOf(
        ShowBehindTrackerPopup()
      )
    else {
      arrayOf()
    }
  }

  override fun hideIfNoVisibleChildren(): Boolean = true
}
