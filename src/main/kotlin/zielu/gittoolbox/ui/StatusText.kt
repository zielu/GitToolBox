package zielu.gittoolbox.ui

import zielu.gittoolbox.ResBundle
import zielu.gittoolbox.status.GitAheadBehindCount
import zielu.gittoolbox.status.Status
import zielu.intellij.util.ZResBundle

internal object StatusText {
  @JvmStatic
  fun format(aheadBehind: GitAheadBehindCount): String {
    val status = aheadBehind.status()
    return if (status.isValid()) {
      if (status === Status.NO_REMOTE) {
        ResBundle.message("git.no.remote")
      } else {
        StatusMessagesService.getInstance().aheadBehindStatus(aheadBehind)
      }
    } else {
      ZResBundle.na()
    }
  }

  @JvmStatic
  fun format(extendedRepoInfo: ExtendedRepoInfo): String {
    return StatusMessagesService.getInstance().extendedRepoInfo(extendedRepoInfo)
  }

  @JvmStatic
  fun formatToolTip(aheadBehind: GitAheadBehindCount): String {
    return if (aheadBehind.status() === Status.SUCCESS) {
      ""
    } else {
      StatusMessagesService.getInstance().aheadBehindStatus(aheadBehind)
    }
  }
}
