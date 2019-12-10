package zielu.gittoolbox.ui

import zielu.gittoolbox.ResBundle
import zielu.gittoolbox.status.GitAheadBehindCount
import zielu.gittoolbox.status.Status

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
      ResBundle.na()
    }
  }

  @JvmStatic
  fun format(extendedRepoInfo: ExtendedRepoInfo): String {
    val parts = ArrayList<String>()
    val changedCount = extendedRepoInfo.changedCount
    if (!changedCount.isEmpty()) {
      parts.add(ResBundle.message("change.count.x.changes.label", changedCount.value))
    }
    return parts.joinToString(" ")
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
