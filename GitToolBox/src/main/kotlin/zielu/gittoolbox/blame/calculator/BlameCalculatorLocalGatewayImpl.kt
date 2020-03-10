package zielu.gittoolbox.blame.calculator

import com.intellij.openapi.project.Project
import git4idea.commands.Git
import git4idea.commands.GitCommand
import git4idea.commands.GitCommandResult
import git4idea.commands.GitLineHandler
import git4idea.repo.GitRepository

internal class BlameCalculatorLocalGatewayImpl(private val project: Project) : BlameCalculatorLocalGateway {

  override fun createLineHandler(repository: GitRepository): GitLineHandler {
    return GitLineHandler(project, repository.root, GitCommand.BLAME)
  }

  override fun runCommand(lineHandler: GitLineHandler): GitCommandResult {
    return Git.getInstance().runCommandWithoutCollectingOutput(lineHandler)
  }
}
