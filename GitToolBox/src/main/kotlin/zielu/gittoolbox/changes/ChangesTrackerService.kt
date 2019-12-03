package zielu.gittoolbox.changes

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.Change
import com.intellij.util.messages.Topic
import zielu.gittoolbox.util.AppUtil

internal interface ChangesTrackerService {

  fun changeListChanged(id: String, changes: Collection<Change>)

  fun getChangesCount(): Int
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
