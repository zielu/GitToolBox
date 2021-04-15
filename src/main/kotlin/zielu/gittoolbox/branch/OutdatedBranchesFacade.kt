package zielu.gittoolbox.branch

import com.intellij.openapi.project.Project
import git4idea.repo.GitRepository

internal class OutdatedBranchesFacade(private val project: Project) {
  fun findNotMergedBranches(repo: GitRepository): Set<String> {
    val calculator = MergedBranchesCalculator(project)
    return calculator.findNotMergedBranches(repo)
  }
}
