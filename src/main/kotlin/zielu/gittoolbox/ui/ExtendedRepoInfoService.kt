package zielu.gittoolbox.ui

import com.intellij.openapi.project.Project
import git4idea.repo.GitRepository
import zielu.gittoolbox.changes.ChangesTrackerService.Companion.getInstance
import zielu.gittoolbox.config.AppConfig
import zielu.gittoolbox.util.AppUtil

internal class ExtendedRepoInfoService {

  fun getExtendedRepoInfo(repo: GitRepository): ExtendedRepoInfo {
    return if (trackingEnabled()) {
      val changesCount = getInstance(repo.project).getChangesCount(repo)
      ExtendedRepoInfo(changesCount)
    } else {
      ExtendedRepoInfo()
    }
  }

  private fun trackingEnabled(): Boolean = AppConfig.getConfig().isChangesTrackingEnabled()

  fun getExtendedRepoInfo(project: Project): ExtendedRepoInfo {
    return if (trackingEnabled()) {
      ExtendedRepoInfo(getInstance(project).getTotalChangesCount())
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
