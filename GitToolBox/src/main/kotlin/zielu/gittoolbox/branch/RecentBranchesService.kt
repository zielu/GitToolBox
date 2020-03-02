package zielu.gittoolbox.branch

import com.intellij.openapi.project.Project
import git4idea.GitBranch
import git4idea.repo.GitRepository
import zielu.gittoolbox.store.RecentBranch
import zielu.gittoolbox.store.WorkspaceStore
import zielu.gittoolbox.util.AppUtil
import java.time.Instant

internal class RecentBranchesService {
  private val historyLimit = 5

  fun branchSwitch(previousBranch: GitBranch, currentBranch: GitBranch, repository: GitRepository) {
    val now = Instant.now()
    val recentBranches = getRecentBranchesFromStore(repository)
    if (recentBranches.isEmpty()) {
      val previous = createRecentBranch(previousBranch, now.minusSeconds(1))
      val current = createRecentBranch(currentBranch, now)
      storeRecentBranches(listOf(current, previous), repository)
    } else {
      updateRecentBranches(createRecentBranch(currentBranch, now), repository)
    }
  }

  fun branchSwitchFromOther(currentBranch: GitBranch, repository: GitRepository) {
    val current = createRecentBranch(currentBranch, Instant.now())
    val recentBranches = getRecentBranchesFromStore(repository)
    if (recentBranches.isEmpty()) {
      storeRecentBranches(listOf(current), repository)
    } else {
      updateRecentBranches(current, repository)
    }
  }

  fun branchSwitchToOther(previousBranch: GitBranch, repository: GitRepository) {
    val previous = createRecentBranch(previousBranch, Instant.now())
    val recentBranches = getRecentBranchesFromStore(repository)
    if (recentBranches.isEmpty()) {
      storeRecentBranches(listOf(previous), repository)
    } else {
      updateRecentBranches(previous, repository)
    }
  }

  private fun createRecentBranch(branch: GitBranch, instant: Instant): RecentBranch {
    return RecentBranch(branch.name, instant.epochSecond)
  }

  private fun getRepoUrl(repository: GitRepository): String {
    return repository.root.url
  }

  private fun getRecentBranchesFromStore(repository: GitRepository): List<RecentBranch> {
    synchronized(this) {
      val store = WorkspaceStore.getInstance(repository.project)
      return store.recentBranches.findForRepositoryRootUrl(getRepoUrl(repository))
    }
  }

  private fun storeRecentBranches(recentBranches: List<RecentBranch>, repository: GitRepository) {
    synchronized(this) {
      val store = WorkspaceStore.getInstance(repository.project)
      store.recentBranches.storeForRepositoryRootUrl(recentBranches, getRepoUrl(repository))
    }
  }

  private fun updateRecentBranches(latestBranch: RecentBranch, repository: GitRepository) {
    synchronized(this) {
      val recentBranches = getRecentBranchesFromStore(repository).toMutableList()
      val branchesCollection = repository.branches
      val deletedBranches = recentBranches
        .filter { branchesCollection.findLocalBranch(it.branchName) == null }
      recentBranches.removeAll(deletedBranches)
      recentBranches.add(latestBranch)
      recentBranches
        .sortByDescending { it.lastUsedInstant }
      recentBranches.take(historyLimit)
      storeRecentBranches(recentBranches, repository)
    }
  }

  fun getRecentBranches(repository: GitRepository): List<GitBranch> {
    val recentBranches = getRecentBranchesFromStore(repository)
    val branchesCollection = repository.branches
    return recentBranches
      .sortedByDescending { it.lastUsedInstant }
      .mapNotNull { branchesCollection.findLocalBranch(it.branchName) }
  }

  companion object {
    fun getInstance(project: Project): RecentBranchesService {
      return AppUtil.getServiceInstance(project, RecentBranchesService::class.java)
    }
  }
}
