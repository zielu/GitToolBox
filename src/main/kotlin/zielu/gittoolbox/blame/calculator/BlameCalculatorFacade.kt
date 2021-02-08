package zielu.gittoolbox.blame.calculator

import com.intellij.openapi.project.Project
import git4idea.commands.Git
import git4idea.commands.GitCommand
import git4idea.commands.GitCommandResult
import git4idea.commands.GitLineHandler
import git4idea.repo.GitRepository
import zielu.gittoolbox.metrics.ProjectMetrics
import zielu.intellij.metrics.GtTimer

internal open class BlameCalculatorFacade(private val project: Project) {

  fun createLineHandler(repository: GitRepository): GitLineHandler {
    return GitLineHandler(project, repository.root, GitCommand.BLAME)
  }

  fun runCommand(lineHandler: GitLineHandler): GitCommandResult {
    return Git.getInstance().runCommandWithoutCollectingOutput(lineHandler)
  }

  fun annotateTimer(): GtTimer {
    return ProjectMetrics.getInstance(project).timer("blame-calculator.annotate")
  }
}
