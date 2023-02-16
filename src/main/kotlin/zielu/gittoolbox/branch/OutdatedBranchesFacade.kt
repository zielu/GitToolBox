package zielu.gittoolbox.branch

import com.intellij.openapi.project.Project
import com.intellij.vcs.log.Hash
import git4idea.GitLocalBranch
import git4idea.repo.GitRepository
import zielu.gittoolbox.config.ProjectConfig
import zielu.gittoolbox.revision.RevisionIndexService
import java.time.ZonedDateTime

internal class OutdatedBranchesFacade(private val project: Project) {
  fun findNotMergedBranches(repo: GitRepository): Set<String> {
    val calculator = MergedBranchesCalculator(project)
    return calculator.findNotMergedBranches(repo)
  }

  fun findMergedBranches(repo: GitRepository): Set<String> {
    val calculator = MergedBranchesCalculator(project)
    return calculator.findMergedBranches(repo)
  }

  fun getLatestCommitTimestamp(repo: GitRepository, branch: GitLocalBranch): ZonedDateTime? {
    val latestCommit = getBranchHash(repo, branch)
    val accessor = RevisionIndexService.getInstance(project).getAccessor(latestCommit, repo.root)
    return accessor?.getAuthorDateTime()
  }

  fun getBranchHash(repo: GitRepository, branch: GitLocalBranch): Hash {
    return repo.branches.getHash(branch)!!
  }

  fun getExclusions(): OutdatedBranchesExclusions {
    val globs = ProjectConfig.getMerged(project).outdatedBranchesCleanupExclusionGlobs()
    return OutdatedBranchesExclusions(globs)
  }
}
