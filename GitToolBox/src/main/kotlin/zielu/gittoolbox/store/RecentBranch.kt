package zielu.gittoolbox.store

import com.intellij.util.xmlb.annotations.Transient

internal data class RecentBranch(
  var branchName: String = "",
  var lastUsedInstant: Long = 0
) {

  @Transient
  fun copy(): RecentBranch {
    return RecentBranch(
      branchName,
      lastUsedInstant
    )
  }
}
