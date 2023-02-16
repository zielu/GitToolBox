package zielu.gittoolbox.ui

import zielu.gittoolbox.status.BehindStatus
import zielu.gittoolbox.status.GitAheadBehindCount

internal class StatusMessagesServiceFacade {
  fun behindStatus(behindStatus: BehindStatus): String {
    return StatusMessagesUi.getInstance().presenter().behindStatus(behindStatus)
  }

  fun aheadBehindStatus(count: GitAheadBehindCount): String {
    return StatusMessagesUi.getInstance().presenter().aheadBehindStatus(count.ahead.value!!, count.behind.value!!)
  }

  fun extendedRepoInfo(extendedRepoInfo: ExtendedRepoInfo): String {
    return StatusMessagesUi.getInstance().presenter().extendedRepoInfo(extendedRepoInfo)
  }
}
