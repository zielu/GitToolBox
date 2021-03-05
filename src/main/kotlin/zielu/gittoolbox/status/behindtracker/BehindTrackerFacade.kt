package zielu.gittoolbox.status.behindtracker

import com.intellij.openapi.project.Project
import git4idea.repo.GitRepository
import zielu.gittoolbox.repo.GtRepository
import zielu.gittoolbox.status.BehindStatus
import zielu.gittoolbox.ui.behindtracker.BehindTrackerUi
import zielu.gittoolbox.util.PrjBaseFacade

internal open class BehindTrackerFacade(private val project: Project) : PrjBaseFacade(project) {
  fun displaySuccessNotification(message: String) {
    BehindTrackerUi.getInstance(project).displaySuccessNotification(message)
  }

  fun prepareBehindMessage(statuses: Map<GitRepository, BehindStatus>, showRepoNames: Boolean): String {
    return BehindTrackerUi.getInstance(project).statusMessages.prepareBehindMessage(statuses, showRepoNames)
  }

  fun getGtRepository(repository: GitRepository): GtRepository {
    return BehindTrackerUi.getInstance(project).getGtRepository(repository)
  }

  fun isNotificationEnabled(): Boolean {
    return BehindTrackerUi.getInstance(project).isNotificationEnabled
  }
}
