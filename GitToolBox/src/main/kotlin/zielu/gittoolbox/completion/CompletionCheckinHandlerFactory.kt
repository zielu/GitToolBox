package zielu.gittoolbox.completion

import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.checkin.CheckinHandler
import zielu.gittoolbox.checkin.GitBaseCheckinHandlerFactory
import zielu.gittoolbox.config.ProjectConfig

internal class CompletionCheckinHandlerFactory : GitBaseCheckinHandlerFactory() {
  override fun createVcsHandler(panel: CheckinProjectPanel): CheckinHandler {
    val config = ProjectConfig.get(panel.project)
    if (config.commitDialogCompletion) {
      val checkinHandler = CompletionCheckinHandler(panel)
      CompletionService.getInstance(panel.project).setScopeProvider(checkinHandler)
      return checkinHandler
    }
    return CheckinHandler.DUMMY
  }
}
