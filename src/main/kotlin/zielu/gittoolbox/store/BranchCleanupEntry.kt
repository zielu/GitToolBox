package zielu.gittoolbox.store

import com.intellij.util.xmlb.annotations.Transient

internal class BranchCleanupEntry(
  var timestamp: Long = 0,
  var deletions: MutableList<BranchDeletion> = mutableListOf()
) {

  @Transient
  fun copy(): BranchCleanupEntry {
    return BranchCleanupEntry(
      timestamp,
      deletions.map { it.copy() }.toMutableList()
    )
  }
}
