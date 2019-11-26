package zielu.gittoolbox.blame.calculator

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.history.VcsRevisionNumber
import com.intellij.openapi.vfs.VirtualFile
import git4idea.GitVcs
import git4idea.commands.Git
import git4idea.commands.GitCommand
import git4idea.commands.GitCommandResult
import git4idea.commands.GitLineHandler
import git4idea.repo.GitRepository

internal class BlameCalculatorLocalGatewayImpl(private val project: Project) : BlameCalculatorLocalGateway {
  override fun getCurrentRevisionNumber(vFile: VirtualFile): VcsRevisionNumber {
    val vcs = GitVcs.getInstance(project)
    return vcs.diffProvider.getCurrentRevision(vFile) ?: VcsRevisionNumber.NULL
  }

  override fun createLineHandler(repository: GitRepository): GitLineHandler {
    return GitLineHandler(project, repository.root, GitCommand.BLAME)
  }

  override fun runCommand(lineHandler: GitLineHandler): GitCommandResult {
    return Git.getInstance().runCommandWithoutCollectingOutput(lineHandler)
  }
}
