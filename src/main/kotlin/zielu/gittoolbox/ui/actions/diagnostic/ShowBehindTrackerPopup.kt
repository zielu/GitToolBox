package zielu.gittoolbox.ui.actions.diagnostic

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import zielu.gittoolbox.ResBundle.message
import zielu.gittoolbox.ui.behindtracker.BehindTrackerUi
import zielu.gittoolbox.util.Html.link

internal class ShowBehindTrackerPopup : AnAction(
  "Show Behind Tracker Popup"
) {

  override fun update(e: AnActionEvent) {
    e.presentation.isVisible = e.project != null
  }

  override fun actionPerformed(e: AnActionEvent) {
    val ui = BehindTrackerUi.getInstance(e.project!!)
    ui.displaySuccessNotification("Behind tracker TEST " + link("update", message("update.project")))
  }
}
