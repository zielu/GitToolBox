package zielu.gittoolbox.store

import com.intellij.util.xmlb.annotations.Transient

internal data class BranchesCleanupHistory(
  var history: MutableList<BranchCleanupEntry> = mutableListOf()
) {

  @Transient
  fun copy(): BranchesCleanupHistory {
    return BranchesCleanupHistory(
      history.map { it.copy() }.toMutableList()
    )
  }
}
