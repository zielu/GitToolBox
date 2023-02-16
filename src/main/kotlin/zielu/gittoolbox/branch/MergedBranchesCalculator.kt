package zielu.gittoolbox.branch

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import git4idea.commands.GitCommand
import git4idea.commands.GitLineHandler
import git4idea.repo.GitRepository

internal class MergedBranchesCalculator(
  private val project: Project
) {
  private val facade = MergedBranchesCalculatorFacade()

  fun findNotMergedBranches(repo: GitRepository): Set<String> {
    val notMerged = runBranch(repo, "--no-merged")
    log.debug("Not merged branches: $notMerged")
    return notMerged
  }

  private fun runBranch(repo: GitRepository, parameter: String): Set<String> {
    val lineHandler = GitLineHandler(project, repo.root, GitCommand.BRANCH)
    lineHandler.addParameters(parameter)
    val result = facade.runCommand(lineHandler)
    return result.output
      .map { it.trim() }
      .filterNot { it.startsWith("*") } // discard current branch
      .toSet()
  }

  fun findMergedBranches(repo: GitRepository): Set<String> {
    val merged = runBranch(repo, "--merged")
    log.debug("Merged branches: $merged")
    return merged
  }

  private companion object {
    private val log = Logger.getInstance(MergedBranchesCalculator::class.java)
  }
}
