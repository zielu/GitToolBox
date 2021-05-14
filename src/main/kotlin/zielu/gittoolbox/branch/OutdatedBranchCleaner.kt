package zielu.gittoolbox.branch

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import git4idea.commands.Git
import git4idea.commands.GitCommandResult
import git4idea.repo.GitRepository
import git4idea.util.GitUIUtil
import jodd.util.StringBand
import zielu.gittoolbox.ResBundle
import zielu.gittoolbox.notification.GtNotifier
import zielu.gittoolbox.store.BranchCleanupEntry
import zielu.gittoolbox.store.BranchDeletion
import zielu.gittoolbox.store.WorkspaceStore
import zielu.gittoolbox.util.GtUtil
import zielu.gittoolbox.util.Html
import java.time.Instant

internal class BranchCleaner(
  myProject: Project,
  private val toClean: Map<GitRepository, List<OutdatedBranch>>
) : Task.Backgroundable(
  myProject,
  ResBundle.message("branch.cleanup.cleaner.title")
) {
  private val resultList = mutableListOf<DeleteResult>()
  private val deleted = mutableListOf<BranchDeletion>()

  override fun run(indicator: ProgressIndicator) {
    indicator.isIndeterminate = true

    val total = toClean.map { it.value.size }.sum()
    val fraction = (1f / total).toDouble()
    var index = 0

    indicator.isIndeterminate = false
    val git = Git.getInstance()
    toClean.forEach { entry ->
      val repoName = GtUtil.name(entry.key)
      entry.value.forEach { branch ->
        indicator.checkCanceled()
        indicator.text = ResBundle.message("branch.cleanup.cleaner.progress", repoName, branch.getName())
        val gitResult = git.branchDelete(entry.key, branch.getName(), forceDelete(branch))
        resultList.add(DeleteResult(repoName, branch, gitResult))
        index++
        indicator.fraction = fraction * index
      }
    }
    indicator.isIndeterminate = true
    toClean.keys.forEach { it.update() }
  }

  private fun forceDelete(branch: OutdatedBranch): Boolean {
    return branch.reason != OutdatedReason.MERGED
  }

  override fun onSuccess() {
    val multipleRepos = toClean.keys.size > 1
    val message = StringBand()
    message
      .append(ResBundle.message("branch.cleanup.cleaner.success.title", resultList.size))
      .append(":")
    resultList
      .filter { it.result.success() }
      .forEach {
        deleted.add(BranchDeletion(it.branch.getName(), it.branch.localHash))
        append(message, it, multipleRepos)
      }

    GtNotifier.getInstance(project).branchCleanupSuccess(
      GitUIUtil.bold(ResBundle.message("branch.cleanup.cleaner.success.message")),
      message.toString()
    )
  }

  private fun append(message: StringBand, result: DeleteResult, multipleRepos: Boolean) {
    message.append(Html.BRX)
    if (multipleRepos) {
      message
        .append(result.repoName)
        .append(": ")
    }
    message.append(result.branch.getName())
  }

  override fun onFinished() {
    if (deleted.isNotEmpty()) {
      val historyEntry = BranchCleanupEntry(Instant.now().toEpochMilli(), deleted.map { it.copy() }.toMutableList())
      WorkspaceStore.get(project).branchesCleanupHistory.history.add(historyEntry)
    }
  }
}

private data class DeleteResult(
  val repoName: String,
  val branch: OutdatedBranch,
  val result: GitCommandResult
)
