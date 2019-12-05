package zielu.gittoolbox.changes

import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic
import git4idea.repo.GitRepository
import zielu.gittoolbox.util.AppUtil
import zielu.gittoolbox.util.Count

internal interface ChangesTrackerService {

  fun changeListChanged(changeListData: ChangeListData)

  fun getChangesCount(repository: GitRepository): Count
  fun changeListRemoved(id: String)

  companion object {
    @JvmField
    val CHANGES_TRACKER_TOPIC: Topic<ChangesTrackerListener> = Topic.create("Git ToolBox Changes Notification",
      ChangesTrackerListener::class.java
    )

    @JvmStatic
    fun getInstance(project: Project): ChangesTrackerService {
      return AppUtil.getServiceInstance(project, ChangesTrackerService::class.java)
    }
  }
}
