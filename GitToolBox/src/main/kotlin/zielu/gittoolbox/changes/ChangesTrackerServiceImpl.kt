package zielu.gittoolbox.changes

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.Change
import com.jetbrains.rd.util.getOrCreate
import git4idea.repo.GitRepository
import gnu.trove.TObjectIntHashMap
import zielu.gittoolbox.util.Count
import java.util.concurrent.ConcurrentHashMap

internal class ChangesTrackerServiceImpl(project: Project) : ChangesTrackerService {
  private val gateway = ChangesTrackerServiceLocalGateway(project)
  private val changeCounters = ConcurrentHashMap<GitRepository, ChangeCounters>()

  override fun changeListChanged(changeListData: ChangeListData) {
    if (changeListData.hasChanges()) {
      gateway.getNotEmptyChangeListTimer().time { handleNonEmptyChangeList(changeListData) }
    } else {
      handleEmptyChangeList(changeListData.id)
    }
  }

  private fun handleEmptyChangeList(id: String) {
    changeListRemoved(id)
  }

  private fun handleNonEmptyChangeList(changeListData: ChangeListData) {
    val newCounts = getCountsForChangeList(changeListData)
    var changed = false
    newCounts.forEach { (repo, aggregator) ->
      val newCounters = aggregator.toCounters()
      val oldCounters = changeCounters.put(repo, newCounters)
      val difference = oldCounters?.getTotal().let { newCounters.getTotal() != it }
      if (difference) {
        changed = true
      }
    }
    if (changed) {
      gateway.fireChangeCountsUpdated()
    }
  }

  private fun getCountsForChangeList(changeListData: ChangeListData): Map<GitRepository, ChangeCountersAggregator> {
    val changeCounters = HashMap<GitRepository, ChangeCountersAggregator>()
    changeListData.changes
      .mapNotNull { getRepoForChange(it) }
      .forEach { changeCounters.getOrCreate(it, { ChangeCountersAggregator() }).increment(changeListData.id) }
    return changeCounters
  }

  private fun getRepoForChange(change: Change): GitRepository? {
    val path = change.afterRevision?.file ?: change.beforeRevision?.file
    return if (path != null) {
      gateway.getRepoForPath(path)
    } else {
      null
    }
  }

  override fun changeListRemoved(id: String) {
    gateway.getChangeListRemovedTimer().time { handleChangeListRemoved(id) }
  }

  private fun handleChangeListRemoved(id: String) {
    val modified = changeCounters.values.map { it.remove(id) }.filter { it }.count()
    if (modified > 0) {
      gateway.fireChangeCountsUpdated()
    }
  }

  override fun getChangesCount(repository: GitRepository): Count {
    return changeCounters[repository]?.getTotal()?.let { Count(it) } ?: Count.EMPTY
  }
}

private class ChangeCountersAggregator {
  private val changeCounters = TObjectIntHashMap<String>()

  fun increment(id: String) {
    if (!changeCounters.increment(id)) {
      changeCounters.put(id, 1)
    }
  }

  fun toCounters(): ChangeCounters = ChangeCounters(changeCounters)
}

private class ChangeCounters(private val changeCounters: TObjectIntHashMap<String>) {
  private var total: Int = 0
  init {
    total = changeCounters.values.sum()
  }

  fun remove(id: String): Boolean {
    synchronized(changeCounters) {
      val removed = changeCounters.remove(id)
      total -= removed
      return removed != 0
    }
  }

  fun getTotal(): Int {
    return total
  }
}
