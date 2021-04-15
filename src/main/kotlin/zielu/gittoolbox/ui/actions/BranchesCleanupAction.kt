package zielu.gittoolbox.ui.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import git4idea.repo.GitRepository
import zielu.gittoolbox.branch.BranchCleaner
import zielu.gittoolbox.branch.OutdatedBranch
import zielu.gittoolbox.branch.OutdatedBranchesService
import zielu.gittoolbox.cache.VirtualFileRepoCache
import zielu.gittoolbox.concurrent.executeAsync
import zielu.gittoolbox.ui.util.AppUiUtil
import zielu.intellij.concurrent.ZCompletableBackgroundable
import java.util.concurrent.CompletableFuture

internal class BranchesCleanupAction : AnAction("Branches Cleanup") {
  override fun actionPerformed(e: AnActionEvent) {
    val project = e.getRequiredData(CommonDataKeys.PROJECT)

    outdatedBranches(project).thenApplyAsync { outdatedBranches ->
      if (outdatedBranches.isNotEmpty()) {
        AppUiUtil.invokeLaterIfNeeded {
          val dialog = CleanBranchesDialog(project)
          dialog.setData(outdatedBranches)
          val performCleanup = dialog.showAndGet()
          if (performCleanup) {
            performCleanup(project, dialog.getData())
          }
        }
      }
    }
  }

  private fun outdatedBranches(project: Project): CompletableFuture<Map<GitRepository, List<OutdatedBranch>>> {
    val repositories = VirtualFileRepoCache.getInstance(project).repositories
    val outdatedBranchesService = OutdatedBranchesService.getInstance(project)

    return object : ZCompletableBackgroundable<Map<GitRepository, List<OutdatedBranch>>>(
      project,
      "Collect outdated branches",
    ) {
      private lateinit var data: Map<GitRepository, List<OutdatedBranch>>

      override fun run(indicator: ProgressIndicator) {
        data = repositories.associateWith {
          indicator.checkCanceled()
          outdatedBranchesService.outdatedBranches(it)
        }.filter { it.value.isNotEmpty() }
      }

      override fun getResult(): Map<GitRepository, List<OutdatedBranch>> = data
    }.executeAsync()
  }

  private fun performCleanup(project: Project, toClean: Map<GitRepository, List<OutdatedBranch>>) {
    BranchCleaner(project, toClean).queue()
  }
}
