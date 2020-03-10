package zielu.gittoolbox.blame.calculator

import git4idea.commands.GitCommandResult
import git4idea.commands.GitLineHandler
import git4idea.repo.GitRepository

internal interface BlameCalculatorLocalGateway {
  fun createLineHandler(repository: GitRepository): GitLineHandler
  fun runCommand(lineHandler: GitLineHandler): GitCommandResult
}
