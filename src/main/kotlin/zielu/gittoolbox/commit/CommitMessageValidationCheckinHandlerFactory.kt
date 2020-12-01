package zielu.gittoolbox.commit

import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.changes.CommitContext
import com.intellij.openapi.vcs.checkin.CheckinHandler
import zielu.gittoolbox.checkin.GitBaseCheckinHandlerFactory

internal class CommitMessageValidationCheckinHandlerFactory : GitBaseCheckinHandlerFactory() {

  override fun createVcsHandler(panel: CheckinProjectPanel, commitContext: CommitContext): CheckinHandler {
    return CommitMessageValidationCheckinHandler(panel)
  }
}
