package zielu.gittoolbox.ui.statusbar

import com.intellij.dvcs.ui.LightActionGroup
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.actionSystem.Separator
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Conditions
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.popup.PopupFactoryImpl.ActionGroupPopup
import zielu.gittoolbox.ResBundle.message
import zielu.gittoolbox.ui.statusbar.StatusBarActions.actionsFor
import zielu.gittoolbox.ui.statusbar.actions.RefreshStatusAction
import zielu.gittoolbox.ui.statusbar.actions.UpdateAction
import zielu.gittoolbox.util.GtUtil
import zielu.gittoolbox.util.GtUtil.getRepositories
import zielu.gittoolbox.util.GtUtil.sort
import java.awt.Component

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
      val projectContext = SimpleDataContext.getProjectContext(project)
      if (PlatformDataKeys.CONTEXT_COMPONENT.getData(projectContext) == null) {
        log.warn("Missing context component, attempting a find it in project IdeFocusManager")
        val prjFocusManager = IdeFocusManager.getInstance(project)
        val prjComponent: Component? = prjFocusManager.focusOwner

        return if (prjComponent != null) {
          wrapComponentIntoContext(prjComponent, projectContext)
        } else {
          log.warn("Missing project focus owner, use project frame")
          val projectFrame: Component? = WindowManager.getInstance().getFrame(project)
          if (projectFrame != null) {
            wrapComponentIntoContext(projectFrame, projectContext)
          } else {
            log.warn("Missing context component")
            projectContext
          }
        }
      } else {
        return projectContext
      }
    }

    private fun wrapComponentIntoContext(component: Component, parent: DataContext): DataContext {
      return SimpleDataContext.getSimpleContext(
        mapOf(
          PlatformDataKeys.CONTEXT_COMPONENT.name to component
        ),
        parent
      )
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
