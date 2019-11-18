package zielu.gittoolbox.completion

import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.checkin.CheckinHandler
import com.intellij.openapi.vcs.checkin.VcsCheckinHandlerFactory
import git4idea.GitVcs
import zielu.gittoolbox.config.GitToolBoxConfigPrj

internal class CompletionCheckinHandlerFactory : VcsCheckinHandlerFactory(GitVcs.getKey()) {
  override fun createVcsHandler(panel: CheckinProjectPanel): CheckinHandler {
    val config = GitToolBoxConfigPrj.getInstance(panel.project)
    if (config.commitDialogCompletion) {
      val checkinHandler = CompletionCheckinHandler(panel)
      CompletionService.getInstance(panel.project).setScopeProvider(checkinHandler)
      return checkinHandler
    }
    return CheckinHandler.DUMMY
  }
}
