package zielu.gittoolbox.branch

import com.intellij.openapi.project.Project
import com.intellij.serviceContainer.NonInjectable
import git4idea.GitBranch
import zielu.gittoolbox.repo.GtRepository
import zielu.gittoolbox.store.RecentBranch
import zielu.gittoolbox.util.AppUtil
import java.time.Instant

internal class RecentBranchesService

@NonInjectable
constructor(
  private val gateway: RecentBranchesLocalGateway
) {

  constructor() : this(RecentBranchesLocalGateway())

  fun branchSwitch(previousBranch: GitBranch, currentBranch: GitBranch, repository: GtRepository) {
    val now = gateway.now()
    val recentBranches = gateway.getRecentBranchesFromStore(repository)
    if (recentBranches.isEmpty()) {
      val previous = createRecentBranch(previousBranch, now.minusSeconds(1))
      val current = createRecentBranch(currentBranch, now)
      gateway.storeRecentBranches(listOf(current, previous), repository)
    } else {
      updateRecentBranches(createRecentBranch(currentBranch, now), repository)
    }
  }

  fun switchToBranchFromOther(currentBranch: GitBranch, repository: GtRepository) {
    val current = createRecentBranch(currentBranch, gateway.now())
    val recentBranches = gateway.getRecentBranchesFromStore(repository)
    if (recentBranches.isEmpty()) {
      gateway.storeRecentBranches(listOf(current), repository)
    } else {
      updateRecentBranches(current, repository)
    }
  }

  fun switchFromBranchToOther(previousBranch: GitBranch, repository: GtRepository) {
    val previous = createRecentBranch(previousBranch, gateway.now())
    val recentBranches = gateway.getRecentBranchesFromStore(repository)
    if (recentBranches.isEmpty()) {
      gateway.storeRecentBranches(listOf(previous), repository)
    } else {
      updateRecentBranches(previous, repository)
    }
  }

  private fun createRecentBranch(branch: GitBranch, instant: Instant): RecentBranch {
    return RecentBranch(branch.name, instant.epochSecond)
  }

  private fun updateRecentBranches(latestBranch: RecentBranch, repository: GtRepository) {
    synchronized(this) {
      val recentBranches = gateway.getRecentBranchesFromStore(repository).toMutableList()
      recentBranches.removeIf { repository.findLocalBranch(it.branchName) == null }
      recentBranches.removeIf { it.branchName == latestBranch.branchName }
      recentBranches.add(latestBranch)
      recentBranches
        .sortByDescending { it.lastUsedInstant }
      val trimmedToLimit = recentBranches.take(HISTORY_LIMIT)
      gateway.storeRecentBranches(trimmedToLimit, repository)
    }
  }

  fun getRecentBranches(repository: GtRepository): List<GitBranch> {
    val recentBranches = gateway.getRecentBranchesFromStore(repository)
    return recentBranches
      .sortedBy { it.lastUsedInstant }
      .mapNotNull { repository.findLocalBranch(it.branchName) }
  }

  companion object {
    private const val HISTORY_LIMIT = 5

    fun getInstance(project: Project): RecentBranchesService {
      return AppUtil.getServiceInstance(project, RecentBranchesService::class.java)
    }
  }
}
