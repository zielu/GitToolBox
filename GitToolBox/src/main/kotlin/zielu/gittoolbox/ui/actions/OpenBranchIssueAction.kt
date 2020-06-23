package zielu.gittoolbox.ui.actions

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import zielu.gittoolbox.ResBundle
import java.util.function.Supplier

internal class OpenBranchIssueAction(
  text: String,
  private val url: String
) : AnAction(Supplier { ResBundle.message("open.branch.issue.action.text", text) }) {

    override fun update(e: AnActionEvent) {
        super.update(e)
        e.presentation.isVisible = e.project != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        BrowserUtil.browse(url, e.project)
    }
}
