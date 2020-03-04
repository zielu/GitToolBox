package zielu.gittoolbox.store

import com.intellij.util.xmlb.annotations.Transient

internal data class RecentBranchesForRepo(
  var repositoryRootUrl: String = "",
  var branches: MutableList<RecentBranch> = arrayListOf()
) {
  @Transient
  fun copy(): RecentBranchesForRepo {
    return RecentBranchesForRepo(
      repositoryRootUrl,
      branches.map { it.copy() }.toMutableList()
    )
  }
}
