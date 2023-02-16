package zielu.gittoolbox.ui.statusbar

import com.intellij.openapi.actionSystem.AnAction
import git4idea.repo.GitRepository
import zielu.gittoolbox.ui.statusbar.actions.FetchAction

internal object StatusBarActions {
  @JvmStatic
  fun actionsFor(repository: GitRepository): List<AnAction> {
    return listOf(FetchAction(repository))
  }
}
