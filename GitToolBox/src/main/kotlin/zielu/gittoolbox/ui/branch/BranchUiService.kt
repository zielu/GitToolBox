package zielu.gittoolbox.ui.branch

import com.intellij.openapi.project.Project
import git4idea.repo.GitRepository
import zielu.gittoolbox.branch.RecentBranchesService
import zielu.gittoolbox.util.AppUtil

internal class BranchUiService(private val project: Project) {
  fun showRecentBranchesSwitcher(repository: GitRepository) {
    val recentBranchesService = RecentBranchesService.getInstance(project)
  }

  fun showRecentBranchesSwitcher(repository: List<GitRepository>) {
    val recentBranchesService = RecentBranchesService.getInstance(project)
  }

  companion object {
    fun getInstance(project: Project): BranchUiService {
      return AppUtil.getServiceInstance(project, BranchUiService::class.java)
    }
  }
}
