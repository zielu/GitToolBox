package zielu.gittoolbox.store

import com.intellij.util.xmlb.annotations.Transient
import java.time.Instant

internal class BranchCleanupEntry(
  var timestamp: Long = 0,
  var deletions: MutableList<BranchDeletion> = mutableListOf()
) {

  constructor(timestamp: Instant) : this(timestamp.toEpochMilli())

  @Transient
  fun getTimestampInstant(): Instant {
    return Instant.ofEpochMilli(timestamp)
  }

  @Transient
  fun copy(): BranchCleanupEntry {
    return BranchCleanupEntry(
      timestamp,
      deletions.map { it.copy() }.toMutableList()
    )
  }
}
