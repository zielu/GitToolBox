package zielu.gittoolbox.ui

import zielu.gittoolbox.status.BehindStatus
import zielu.gittoolbox.status.GitAheadBehindCount

internal class StatusMessagesServiceLocalGatewayImpl : StatusMessagesServiceLocalGateway {
  override fun behindStatus(behindStatus: BehindStatus): String {
    return StatusMessagesUi.getInstance().presenter().behindStatus(behindStatus)
  }

  override fun aheadBehindStatus(count: GitAheadBehindCount): String {
    return StatusMessagesUi.getInstance().presenter().aheadBehindStatus(count.ahead.value(), count.behind.value());
  }
}
