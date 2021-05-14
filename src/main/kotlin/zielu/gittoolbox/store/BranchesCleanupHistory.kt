package zielu.gittoolbox.store

import com.intellij.util.xmlb.annotations.Transient
import java.time.Instant
import java.time.temporal.ChronoUnit

internal data class BranchesCleanupHistory(
  var history: MutableList<BranchCleanupEntry> = mutableListOf()
) {

  fun append(entry: BranchCleanupEntry) {
    synchronized(this) {
      pruneOldEntries()
      history.add(entry)
    }
  }

  private fun pruneOldEntries() {
    val cutOff = Instant.now().minus(30, ChronoUnit.DAYS)
    history = history.filter { it.getTimestampInstant().isAfter(cutOff) }.toMutableList()
  }

  @Transient
  fun copy(): BranchesCleanupHistory {
    synchronized(this) {
      return BranchesCleanupHistory(
        history.map { it.copy() }.toMutableList()
      )
    }
  }
}
