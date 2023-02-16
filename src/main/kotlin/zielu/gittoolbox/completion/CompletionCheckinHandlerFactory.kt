package zielu.gittoolbox.completion

import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.changes.CommitContext
import com.intellij.openapi.vcs.checkin.CheckinHandler
import zielu.gittoolbox.checkin.GitBaseCheckinHandlerFactory
import zielu.gittoolbox.config.ProjectConfig

internal class CompletionCheckinHandlerFactory : GitBaseCheckinHandlerFactory() {

  override fun createVcsHandler(panel: CheckinProjectPanel, commitContext: CommitContext): CheckinHandler {
    val config = ProjectConfig.getMerged(panel.project)
    if (config.commitDialogBranchCompletion()) {
      val checkinHandler = CompletionCheckinHandler(panel)
      CompletionService.getInstance(panel.project).setScopeProvider(checkinHandler)
      return checkinHandler
    }
    return CheckinHandler.DUMMY
  }
}
