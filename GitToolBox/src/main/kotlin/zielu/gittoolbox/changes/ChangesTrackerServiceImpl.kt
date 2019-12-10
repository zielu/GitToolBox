package zielu.gittoolbox.changes

import com.intellij.openapi.project.Project
import com.jetbrains.rd.util.getOrCreate
import git4idea.repo.GitRepository
import gnu.trove.TObjectIntHashMap
import zielu.gittoolbox.util.Count
import java.util.concurrent.ConcurrentHashMap

internal class ChangesTrackerServiceImpl(project: Project) : ChangesTrackerService {
  private val gateway = ChangesTrackerServiceLocalGateway(project)
  private val changeCounters = ConcurrentHashMap<GitRepository, ChangeCounters>()

  override fun changeListChanged(changeListData: ChangeListData) {
    if (changeListData.hasChanges) {
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
      val difference = oldCounters?.total.let { newCounters.total != it }
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

  private fun getRepoForChange(change: ChangeData): GitRepository? {
    return gateway.getRepoForPath(change.filePath)
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
    return changeCounters[repository]?.let { Count(it.total) } ?: Count.ZERO
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
  private var totalCount: Int = changeCounters.values.sum()

  val total: Int
    get() = totalCount

  fun remove(id: String): Boolean {
    synchronized(changeCounters) {
      val removed = changeCounters.remove(id)
      totalCount -= removed
      return removed != 0
    }
  }
}
