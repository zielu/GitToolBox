package zielu.gittoolbox.branch

import git4idea.GitLocalBranch
import git4idea.GitRemoteBranch
import java.time.ZonedDateTime

internal data class OutdatedBranch(
  val localBranch: GitLocalBranch,
  val reason: OutdatedReason,
  val latestCommitTimestamp: ZonedDateTime?,
  val remoteBranch: GitRemoteBranch? = null
) {
  fun getName(): String = localBranch.name

  fun hasRemote(): Boolean = remoteBranch != null

  fun getRemoteBranchName(): String? = remoteBranch?.name
}
