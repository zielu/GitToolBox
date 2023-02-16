package zielu.gittoolbox.ui.statusbar

import com.intellij.dvcs.ui.LightActionGroup
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.Separator
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Conditions
import com.intellij.ui.popup.PopupFactoryImpl.ActionGroupPopup
import zielu.gittoolbox.ResBundle.message
import zielu.gittoolbox.ui.statusbar.StatusBarActions.actionsFor
import zielu.gittoolbox.ui.statusbar.actions.RefreshStatusAction
import zielu.gittoolbox.ui.statusbar.actions.UpdateAction
import zielu.gittoolbox.ui.util.AppUiUtil
import zielu.gittoolbox.util.GtUtil
import zielu.gittoolbox.util.GtUtil.getRepositories
import zielu.gittoolbox.util.GtUtil.sort

internal class StatusActionGroupPopup(
  title: String,
  myProject: Project
) : ActionGroupPopup(
  title,
  createActions(myProject),
  createDataContext(myProject),
  false,
  false,
  true,
  false,
  null,
  -1,
  Conditions.alwaysTrue(),
  null
) {

  private companion object {
    private val log = Logger.getInstance(StatusActionGroupPopup::class.java)

    fun createDataContext(project: Project): DataContext {
      return AppUiUtil.prepareDataContextWithContextComponent(project)
    }

    fun createActions(project: Project): ActionGroup {
      val actionGroup = LightActionGroup()
      actionGroup.add(RefreshStatusAction())
      actionGroup.add(UpdateAction())
      actionGroup.addAll(createPerRepositoryActions(project))
      return actionGroup
    }

    private fun createPerRepositoryActions(project: Project): List<AnAction> {
      val actions = mutableListOf<AnAction>()
      var repositories = getRepositories(project)
      repositories = sort(repositories)
      val repos = repositories.filter { GtUtil.hasRemotes(it) }
      if (repos.isNotEmpty()) {
        actions.add(Separator.create(message("statusBar.status.menu.repositories.title")))
        if (repos.size == 1) {
          val repo = repos[0]
          actions.addAll(actionsFor(repo))
        } else {
          actions.addAll(repos.map { RepositoryActions(it) })
        }
      }
      return actions
    }
  }
}
