package zielu.gittoolbox.branch

import com.intellij.openapi.project.Project
import com.intellij.serviceContainer.NonInjectable
import git4idea.GitLocalBranch
import git4idea.GitRemoteBranch
import git4idea.repo.GitRepository
import zielu.gittoolbox.util.AppUtil

internal class OutdatedBranchesService
@NonInjectable
constructor(private val facade: OutdatedBranchesFacade) {

  constructor(project: Project) : this(OutdatedBranchesFacade(project))

  fun outdatedBranches(repo: GitRepository): List<OutdatedBranch> {
    val branches = repo.branches.localBranches.map {
      Branch(it, it.findTrackedBranch(repo))
    }
    val notMerged = facade.findNotMergedBranches(repo)
    val merged = facade.findMergedBranches(repo)

    val currentBranch = repo.currentBranch
    val exclusions = facade.getExclusions()

    return branches
      .filterNot { it.local == currentBranch }
      .filterNot {
        if (it.remote != null) {
          it.local.name in notMerged
        } else {
          false
        }
      }
      .filterNot { exclusions.isExcluded(it.local.name) }
      .map { createOutdated(repo, it, merged) }
    // TODO: add filtering of commits older than X days
  }

  private fun createOutdated(repo: GitRepository, branch: Branch, merged: Set<String>): OutdatedBranch {
    val lastCommitTimestamp = facade.getLatestCommitTimestamp(repo, branch.local)
    val reason = if (branch.local.name in merged) OutdatedReason.MERGED else OutdatedReason.OLD_COMMIT
    return OutdatedBranch(branch.local, reason, lastCommitTimestamp, branch.remote)
  }

  companion object {
    fun getInstance(project: Project): OutdatedBranchesService {
      return AppUtil.getServiceInstance(project, OutdatedBranchesService::class.java)
    }
  }
}

private data class Branch(
  val local: GitLocalBranch,
  val remote: GitRemoteBranch?
)
