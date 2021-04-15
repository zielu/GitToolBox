package zielu.gittoolbox.ui.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import git4idea.repo.GitRepository
import git4idea.util.GitUIUtil
import zielu.gittoolbox.branch.BranchCleaner
import zielu.gittoolbox.branch.OutdatedBranch
import zielu.gittoolbox.branch.OutdatedBranchesCollector
import zielu.gittoolbox.concurrent.executeAsync
import zielu.gittoolbox.notification.GtNotifier
import zielu.gittoolbox.ui.util.AppUiUtil
import java.util.concurrent.CompletableFuture

internal class OutdatedBranchesCleanupAction : AnAction("Git Branches Cleanup") {
  override fun actionPerformed(e: AnActionEvent) {
    val project = e.getRequiredData(CommonDataKeys.PROJECT)

    outdatedBranches(project).thenAccept { outdatedBranches ->
      if (outdatedBranches.isNotEmpty()) {
        AppUiUtil.invokeLaterIfNeeded {
          val dialog = OutdatedBranchesDialog(project)
          dialog.setData(outdatedBranches)
          val performCleanup = dialog.showAndGet()
          if (performCleanup) {
            performCleanup(project, dialog.getData())
          }
        }
      } else {
        GtNotifier.getInstance(project).branchCleanupSuccess(
          GitUIUtil.bold("Git Branch Cleanup"),
          "No outdated branches found"
        )
      }
    }
  }

  private fun outdatedBranches(project: Project): CompletableFuture<Map<GitRepository, List<OutdatedBranch>>> {
    return OutdatedBranchesCollector(project).executeAsync()
  }

  private fun performCleanup(project: Project, toClean: Map<GitRepository, List<OutdatedBranch>>) {
    BranchCleaner(project, toClean).queue()
  }
}
