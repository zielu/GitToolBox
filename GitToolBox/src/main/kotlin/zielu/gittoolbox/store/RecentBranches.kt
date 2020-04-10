package zielu.gittoolbox.store

import com.intellij.util.xmlb.annotations.Transient

internal data class RecentBranches(
  var branchesForRepo: MutableList<RecentBranchesForRepo> = ArrayList()
) {

  fun findForRepositoryRootUrl(repositoryRootUrl: String): List<RecentBranch> {
    synchronized(this) {
      return branchesForRepo.asSequence()
        .filter { it.repositoryRootUrl == repositoryRootUrl }
        .flatMap { it.branches.asSequence() }
        .toList()
    }
  }

  fun storeForRepositoryRootUrl(recentBranches: List<RecentBranch>, repositoryRootUrl: String) {
    synchronized(this) {
      branchesForRepo.removeIf { it.repositoryRootUrl == repositoryRootUrl }
      branchesForRepo.add(RecentBranchesForRepo(repositoryRootUrl, recentBranches.toMutableList()))
    }
  }

  @Transient
  fun copy(): RecentBranches {
    synchronized(this) {
      return RecentBranches(
        branchesForRepo.map { it.copy() }.toMutableList()
      )
    }
  }
}
