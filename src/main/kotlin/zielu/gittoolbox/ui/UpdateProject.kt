package zielu.gittoolbox.ui

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.jetbrains.concurrency.Promise
import zielu.gittoolbox.config.AppConfig.Companion.getConfig
import zielu.gittoolbox.ui.update.UpdateProjectActionService
import zielu.gittoolbox.ui.util.AppUiUtil
import java.awt.event.InputEvent

internal object UpdateProject {
  private val log = Logger.getInstance(UpdateProject::class.java)

  @JvmStatic
  fun execute(project: Project, event: AnActionEvent) {
    AppUiUtil.invokeLater(project) { invokeAction(event) }
  }

  @JvmStatic
  fun execute(project: Project, inputEvent: InputEvent?) {
    AppUiUtil.invokeLater(project) { invokeAction(inputEvent) }
  }

  private fun invokeAction(event: AnActionEvent) {
    val action = getAction()
    action.actionPerformed(event)
  }

  private fun invokeAction(inputEvent: InputEvent?) {
    val action = getAction()
    synthesiseEvent(action, inputEvent)
      .onSuccess { e: AnActionEvent -> action.actionPerformed(e) }
      .onError { error: Throwable ->
        log.warn("Project update failed", error)
      }
  }

  private fun getAction(): AnAction {
    val actionId = getConfig().updateProjectActionId
    return UpdateProjectActionService.getInstance().getById(actionId).getAction()
  }

  private fun synthesiseEvent(action: AnAction, inputEvent: InputEvent?): Promise<AnActionEvent> {
    val dataManager = DataManager.getInstance()
    return dataManager.dataContextFromFocusAsync
      .then { dataContext: DataContext ->
        AnActionEvent.createFromAnAction(
          action, inputEvent, ActionPlaces.UNKNOWN,
          dataContext
        )
      }
  }
}
