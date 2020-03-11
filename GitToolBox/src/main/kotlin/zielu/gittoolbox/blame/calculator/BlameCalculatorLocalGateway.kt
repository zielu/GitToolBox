package zielu.gittoolbox.blame.calculator

import com.codahale.metrics.Timer
import git4idea.commands.GitCommandResult
import git4idea.commands.GitLineHandler
import git4idea.repo.GitRepository

internal interface BlameCalculatorLocalGateway {
  fun createLineHandler(repository: GitRepository): GitLineHandler
  fun runCommand(lineHandler: GitLineHandler): GitCommandResult
  fun annotateTimer(): Timer
}
