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
    val lineHandler = GitLineHandler(project, repo.root, GitCommand.BRANCH)
    lineHandler.addParameters("--no-merged")
    val result = facade.runCommand(lineHandler)
    val notMerged = result.output.map { it.trim() }.toSet()
    log.debug("Not merged branches: $notMerged")
    return notMerged
  }

  private companion object {
    private val log = Logger.getInstance(MergedBranchesCalculator::class.java)
  }
}
