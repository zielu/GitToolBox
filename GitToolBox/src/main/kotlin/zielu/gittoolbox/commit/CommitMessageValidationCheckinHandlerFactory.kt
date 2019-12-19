package zielu.gittoolbox.commit

import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.checkin.CheckinHandler
import zielu.gittoolbox.checkin.GitBaseCheckinHandlerFactory
import zielu.gittoolbox.config.GitToolBoxConfigPrj

internal class CommitMessageValidationCheckinHandlerFactory : GitBaseCheckinHandlerFactory() {

  override fun createVcsHandler(panel: CheckinProjectPanel): CheckinHandler {
    val config = GitToolBoxConfigPrj.getInstance(panel.project)
    return CommitMessageValidationCheckinHandler(panel, config.commitMessageValidationRegex)
  }
}
