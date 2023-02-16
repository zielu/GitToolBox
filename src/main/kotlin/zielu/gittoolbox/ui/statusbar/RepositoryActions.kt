package zielu.gittoolbox.ui.statusbar

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import git4idea.repo.GitRepository
import zielu.gittoolbox.util.GtUtil

internal class RepositoryActions(
  private val repository: GitRepository
) : ActionGroup(GtUtil.name(repository), true) {

  override fun getChildren(e: AnActionEvent?): Array<AnAction> {
    return StatusBarActions.actionsFor(repository).toTypedArray()
  }
}
