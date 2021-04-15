package zielu.gittoolbox.branch

import git4idea.commands.Git
import git4idea.commands.GitCommandResult
import git4idea.commands.GitLineHandler

internal class MergedBranchesCalculatorFacade {

  fun runCommand(command: GitLineHandler): GitCommandResult {
    return Git.getInstance().runCommand(command)
  }
}
