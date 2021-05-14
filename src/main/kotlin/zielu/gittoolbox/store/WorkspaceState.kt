package zielu.gittoolbox.store

import com.intellij.util.xmlb.annotations.Transient

internal data class WorkspaceState(
  var recentBranches: RecentBranches = RecentBranches(),
  var branchesCleanupHistory: BranchesCleanupHistory = BranchesCleanupHistory(),
  var projectConfigVersion: Int = 1
) {
  @Transient
  fun copy(): WorkspaceState {
    return WorkspaceState(
      recentBranches.copy(),
      branchesCleanupHistory.copy(),
      projectConfigVersion
    )
  }
}
