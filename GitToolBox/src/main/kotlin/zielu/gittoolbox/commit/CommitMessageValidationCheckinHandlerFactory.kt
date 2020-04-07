package zielu.gittoolbox.commit

import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.checkin.CheckinHandler
import zielu.gittoolbox.checkin.GitBaseCheckinHandlerFactory
import zielu.gittoolbox.config.ProjectConfig

internal class CommitMessageValidationCheckinHandlerFactory : GitBaseCheckinHandlerFactory() {

  override fun createVcsHandler(panel: CheckinProjectPanel): CheckinHandler {
    val config = ProjectConfig.get(panel.project)
    return CommitMessageValidationCheckinHandler(panel, config.commitMessageValidationRegex)
  }
}
