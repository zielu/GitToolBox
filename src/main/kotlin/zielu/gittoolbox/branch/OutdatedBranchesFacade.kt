package zielu.gittoolbox.branch

import com.intellij.openapi.project.Project
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
    val latestCommit = repo.branches.getHash(branch)!!
    val accessor = RevisionIndexService.getInstance(project).getAccessor(latestCommit, repo.root)
    return accessor?.getAuthorDateTime()
  }

  fun getExclusions(): OutdatedBranchesExclusions {
    val globs = ProjectConfig.getMerged(project).outdatedBranchesCleanupExclusionGlobs()
    return OutdatedBranchesExclusions(globs)
  }
}
