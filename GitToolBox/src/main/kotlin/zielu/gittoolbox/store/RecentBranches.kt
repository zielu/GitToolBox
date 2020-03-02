package zielu.gittoolbox.store

import com.intellij.util.xmlb.annotations.Transient

internal data class RecentBranches(
  var branchesForRepo: MutableList<RecentBranchesForRepo> = arrayListOf()
) {

  fun findForRepositoryRootUrl(repositoryRootUrl: String): List<RecentBranch> {
    return branchesForRepo
      .filter { it.repositoryRootUrl == repositoryRootUrl }
      .flatMap { it.branches }
  }

  fun storeForRepositoryRootUrl(recentBranches: List<RecentBranch>, repositoryRootUrl: String) {
    branchesForRepo.removeIf { it.repositoryRootUrl == repositoryRootUrl }
    branchesForRepo.add(RecentBranchesForRepo(repositoryRootUrl, recentBranches.toMutableList()))
  }

  @Transient
  fun copy(): RecentBranches {
    return RecentBranches(
      branchesForRepo.map { it.copy() }.toMutableList()
    )
  }
}
