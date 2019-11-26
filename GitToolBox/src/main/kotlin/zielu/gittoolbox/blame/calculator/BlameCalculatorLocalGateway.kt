package zielu.gittoolbox.blame.calculator

import com.intellij.openapi.vcs.history.VcsRevisionNumber
import com.intellij.openapi.vfs.VirtualFile
import git4idea.commands.GitCommandResult
import git4idea.commands.GitLineHandler
import git4idea.repo.GitRepository

internal interface BlameCalculatorLocalGateway {
  fun getCurrentRevisionNumber(vFile: VirtualFile): VcsRevisionNumber
  fun createLineHandler(repository: GitRepository): GitLineHandler
  fun runCommand(lineHandler: GitLineHandler): GitCommandResult
}
