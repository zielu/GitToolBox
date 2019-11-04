package zielu.gittoolbox.ui

import zielu.gittoolbox.status.BehindStatus
import zielu.gittoolbox.status.GitAheadBehindCount

internal interface StatusMessagesServiceLocalGateway {
  fun behindStatus(behindStatus: BehindStatus): String
  fun aheadBehindStatus(count: GitAheadBehindCount): String
}
