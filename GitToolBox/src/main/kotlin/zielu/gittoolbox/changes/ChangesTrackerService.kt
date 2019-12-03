package zielu.gittoolbox.changes

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.Change
import zielu.gittoolbox.util.AppUtil

internal interface ChangesTrackerService {

  fun changeListChanged(id: String, changes: Collection<Change>)

  fun getChangesCount(): Int

  companion object {
    @JvmStatic
    fun getInstance(project: Project): ChangesTrackerService {
      return AppUtil.getServiceInstance(project, ChangesTrackerService::class.java)
    }
  }
}
