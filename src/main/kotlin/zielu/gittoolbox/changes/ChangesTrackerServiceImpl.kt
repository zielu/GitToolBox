package zielu.gittoolbox.changes

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.serviceContainer.NonInjectable
import com.jetbrains.rd.util.getOrCreate
import git4idea.repo.GitRepository
import gnu.trove.TObjectIntHashMap
import zielu.gittoolbox.util.Count
import zielu.intellij.util.ZDisposeGuard
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

internal class ChangesTrackerServiceImpl
@NonInjectable
constructor(private val facade: ChangesTrackerServiceFacade) : ChangesTrackerService, Disposable {

  constructor(project: Project) : this(ChangesTrackerServiceFacade(project))

  private val changeCounters: ConcurrentMap<GitRepository, ChangeCounters> = ConcurrentHashMap()
  private val disposeGuard = ZDisposeGuard()
  init {
    facade.registerDisposable(this, facade)
    facade.registerDisposable(this, disposeGuard)
  }

  override fun changeListChanged(changeListData: ChangeListData) {
    if (disposeGuard.isActive()) {
      if (changeListData.hasChanges) {
        facade.getNotEmptyChangeListTimer().timeKt { handleNonEmptyChangeList(changeListData) }
      } else {
        handleEmptyChangeList(changeListData.id)
      }
    }
  }

  private fun handleEmptyChangeList(id: String) {
    changeListRemoved(id)
  }

  private fun handleNonEmptyChangeList(changeListData: ChangeListData) {
    val newCounts = getCountsForChangeList(changeListData)

    var changed = false
    // clean up committed changes
    val reposNotInChangeList: MutableSet<GitRepository> = HashSet(changeCounters.keys)
    reposNotInChangeList.removeAll(newCounts.keys)
    reposNotInChangeList.forEach { repoNotInList ->
      changeCounters.computeIfPresent(repoNotInList) { _, counters ->
        if (counters.remove(changeListData.id)) {
          changed = true
        }
        counters
      }
    }

    // update counts
    newCounts.forEach { (repo, aggregator) ->
      val oldTotal = changeCounters[repo]?.total ?: 0
      val newCounters = changeCounters.merge(repo, aggregator.toCounters(), this::mergeCounters)
      val difference = newCounters!!.total != oldTotal
      if (difference) {
        changed = true
      }
    }
    if (changed) {
      facade.fireChangeCountsUpdated()
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
    return facade.getRepoForPath(change.filePath)
  }

  private fun mergeCounters(existingCounters: ChangeCounters, newCounters: ChangeCounters): ChangeCounters {
    return existingCounters.merge(newCounters)
  }

  override fun changeListRemoved(id: String) {
    if (disposeGuard.isActive()) {
      facade.getChangeListRemovedTimer().timeKt { handleChangeListRemoved(id) }
    }
  }

  private fun handleChangeListRemoved(id: String) {
    val modified = changeCounters.values.stream()
      .map { it.remove(id) }
      .filter { it }
      .count()
    if (modified > 0) {
      facade.fireChangeCountsUpdated()
    }
  }

  override fun getChangesCount(repository: GitRepository): Count {
    if (disposeGuard.isActive()) {
      return changeCounters[repository]?.let { Count(it.total) } ?: Count.ZERO
    }
    return Count.ZERO
  }

  override fun getTotalChangesCount(): Count {
    if (disposeGuard.isActive()) {
      val total = changeCounters.values
        .map { it.total }
        .sum()
      return Count(total)
    }
    return Count.ZERO
  }

  override fun dispose() {
    changeCounters.clear()
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

  fun hasId(id: String): Boolean {
    synchronized(changeCounters) {
      return changeCounters.containsKey(id)
    }
  }

  fun merge(other: ChangeCounters): ChangeCounters {
    synchronized(changeCounters) {
      other.changeCounters.forEachEntry { listId, count ->
        changeCounters.put(listId, count)
        true
      }
      totalCount = changeCounters.values.sum()
      return this
    }
  }
}
