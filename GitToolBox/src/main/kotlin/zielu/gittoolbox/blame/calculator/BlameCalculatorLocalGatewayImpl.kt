package zielu.gittoolbox.blame.calculator

import com.codahale.metrics.Timer
import com.intellij.openapi.project.Project
import git4idea.commands.Git
import git4idea.commands.GitCommand
import git4idea.commands.GitCommandResult
import git4idea.commands.GitLineHandler
import git4idea.repo.GitRepository
import zielu.gittoolbox.metrics.ProjectMetrics

internal class BlameCalculatorLocalGatewayImpl(private val project: Project) : BlameCalculatorLocalGateway {

  override fun createLineHandler(repository: GitRepository): GitLineHandler {
    return GitLineHandler(project, repository.root, GitCommand.BLAME)
  }

  override fun runCommand(lineHandler: GitLineHandler): GitCommandResult {
    return Git.getInstance().runCommandWithoutCollectingOutput(lineHandler)
  }

  override fun annotateTimer(): Timer {
    return ProjectMetrics.getInstance(project).timer("blame-calculator.annotate")
  }
}
