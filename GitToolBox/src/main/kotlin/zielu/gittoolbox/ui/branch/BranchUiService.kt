package zielu.gittoolbox.ui.branch

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import zielu.gittoolbox.branch.RecentBranchesService
import zielu.gittoolbox.repo.GtRepository
import zielu.gittoolbox.util.AppUtil

internal class BranchUiService(private val project: Project) {
  fun showRecentBranchesSwitcher(repository: GtRepository) {
    val items = createItems(repository, false)
    showPopup(items)
  }

  fun showRecentBranchesSwitcher(repository: List<GtRepository>) {
    val items = mutableListOf<RecentBranchItem>()
    repository.forEach {
      items.addAll(createItems(it, true))
    }
    showPopup(items)
  }

  private fun createItems(repository: GtRepository, includeRepoName: Boolean): List<RecentBranchItem> {
    val recentBranchesService = RecentBranchesService.getInstance(project)
    val recentBranches = recentBranchesService.getRecentBranches(repository)
    if (recentBranches.isNotEmpty()) {
      return if (includeRepoName) {
        val items = mutableListOf<RecentBranchItem>()
        items.add(createTitledRecentBranchItem(recentBranches[0], repository))
        items.addAll(recentBranches.subList(1, recentBranches.size).map { createRecentBranchItem(it, repository) })
        items
      } else {
        recentBranches.map { createRecentBranchItem(it, repository) }
      }
    }
    return listOf()
  }

  private fun showPopup(items: List<RecentBranchItem>) {
    if (items.isNotEmpty()) {
      val popup = JBPopupFactory.getInstance()
        .createListPopup(RecentBranchesListPopupStep("Recent Git branches", items))
      popup.showInFocusCenter()
    }
  }

  companion object {
    fun getInstance(project: Project): BranchUiService {
      return AppUtil.getServiceInstance(project, BranchUiService::class.java)
    }
  }
}
