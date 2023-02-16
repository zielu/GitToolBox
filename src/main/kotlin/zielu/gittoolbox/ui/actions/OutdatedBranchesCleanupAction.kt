package zielu.gittoolbox.ui.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import git4idea.repo.GitRepository
import git4idea.util.GitUIUtil
import zielu.gittoolbox.ResBundle
import zielu.gittoolbox.branch.BranchCleaner
import zielu.gittoolbox.branch.OutdatedBranch
import zielu.gittoolbox.branch.OutdatedBranchesCollector
import zielu.gittoolbox.concurrent.executeAsync
import zielu.gittoolbox.notification.GtNotifier
import zielu.gittoolbox.ui.branch.OutdatedBranchesDialog
import zielu.gittoolbox.ui.util.AppUiUtil
import java.util.concurrent.CompletableFuture

internal class OutdatedBranchesCleanupAction : AnAction(ResBundle.message("branch.cleanup.action.name")) {
  override fun actionPerformed(e: AnActionEvent) {
    val project = e.getRequiredData(CommonDataKeys.PROJECT)

    outdatedBranches(project)
      .whenComplete { outdatedBranches, error ->
        if (error != null) {
          log.error("Outdated branches calculation failed", error)
        } else {
          handleOutdated(project, outdatedBranches)
        }
      }
  }

  private fun handleOutdated(project: Project, outdatedBranches: Map<GitRepository, List<OutdatedBranch>>) {
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
        GitUIUtil.bold(ResBundle.message("branch.cleanup.notification.success.title")),
        ResBundle.message("branch.cleanup.notification.nothing.found")
      )
    }
  }

  private fun outdatedBranches(project: Project): CompletableFuture<Map<GitRepository, List<OutdatedBranch>>> {
    return OutdatedBranchesCollector(project).executeAsync()
  }

  private fun performCleanup(project: Project, toClean: Map<GitRepository, List<OutdatedBranch>>) {
    BranchCleaner(project, toClean).queue()
  }

  private companion object {
    private val log = Logger.getInstance(OutdatedBranchesCleanupAction::class.java)
  }
}
