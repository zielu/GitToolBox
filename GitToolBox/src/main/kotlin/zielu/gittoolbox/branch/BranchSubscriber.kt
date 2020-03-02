package zielu.gittoolbox.branch

import com.intellij.openapi.project.Project
import git4idea.repo.GitRepository
import zielu.gittoolbox.cache.RepoInfo
import zielu.gittoolbox.util.AppUtil

internal class BranchSubscriber {
  fun onRepoStateChanged(previous: RepoInfo, current: RepoInfo, repository: GitRepository) {
    if (!previous.isEmpty && !current.isEmpty) {
      if (previous.status().localBranch() != null) {
        if (current.status().localBranch() != null) {
          if (!previous.status().sameLocalBranch(current.status())) {
            RecentBranchesService.getInstance(repository.project).branchSwitch(
              previous.status().localBranch()!!,
              current.status().localBranch()!!,
              repository)
          }
        } else {
          RecentBranchesService.getInstance(repository.project).branchSwitchToOther(
            previous.status().localBranch()!!,
            repository)
        }
      } else if (current.status().localBranch() != null) {
        RecentBranchesService.getInstance(repository.project).branchSwitchFromOther(
          current.status().localBranch()!!,
          repository)
      }
    }
  }

  companion object {
    fun getInstance(project: Project): BranchSubscriber {
      return AppUtil.getServiceInstance(project, BranchSubscriber::class.java)
    }
  }
}
