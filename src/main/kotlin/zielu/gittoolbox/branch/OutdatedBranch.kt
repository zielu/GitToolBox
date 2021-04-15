package zielu.gittoolbox.branch

import git4idea.GitLocalBranch
import git4idea.GitRemoteBranch

internal data class OutdatedBranch(
  val localBranch: GitLocalBranch,
  val remoteBranch: GitRemoteBranch?
) {
  fun getName(): String = localBranch.name

  fun hasRemote(): Boolean = remoteBranch != null

  fun getRemoteBranchName(): String? = remoteBranch?.name
}
