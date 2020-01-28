package zielu.gittoolbox.ui

import git4idea.repo.GitRepository
import zielu.gittoolbox.changes.ChangesTrackerService.Companion.getInstance
import zielu.gittoolbox.config.GitToolBoxConfig2
import zielu.gittoolbox.util.AppUtil

internal class ExtendedRepoInfoService {

  fun getExtendedRepoInfo(repo: GitRepository): ExtendedRepoInfo {
    val config = GitToolBoxConfig2.getInstance()
    return if (config.isChangesTrackingEnabled()) {
      val changesCount = getInstance(repo.project).getChangesCount(repo)
      ExtendedRepoInfo(changesCount)
    } else {
      ExtendedRepoInfo()
    }
  }

  companion object {
    @JvmStatic
    fun getInstance(): ExtendedRepoInfoService {
      return AppUtil.getServiceInstance(ExtendedRepoInfoService::class.java)
    }
  }
}
