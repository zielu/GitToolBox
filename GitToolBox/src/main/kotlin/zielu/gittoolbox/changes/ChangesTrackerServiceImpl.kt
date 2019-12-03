package zielu.gittoolbox.changes

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.Change
import gnu.trove.TObjectIntHashMap
import java.util.concurrent.atomic.AtomicInteger

internal class ChangesTrackerServiceImpl(project: Project) : ChangesTrackerService {
  private val gateway = ChangesTrackerServiceLocalGateway(project)
  private val changeCounters = TObjectIntHashMap<String>()
  private val changesCount = AtomicInteger()

  override fun changeListChanged(id: String, changes: Collection<Change>) {
    val newValue = changes.size
    val oldValue = synchronized(changeCounters) { changeCounters.put(id, newValue) }
    if (newValue != oldValue) {
      countersUpdated()
    }
  }

  private fun countersUpdated() {
    val newCount = synchronized(changeCounters) { changeCounters.values.sum() }
    val oldCount = changesCount.get()
    if (newCount != oldCount && changesCount.compareAndSet(oldCount, newCount)) {
      gateway.fireChangesCountUpdated(newCount)
    }
  }

  override fun getChangesCount(): Int {
    return changesCount.get()
  }
}
