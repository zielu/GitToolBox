package zielu.gittoolbox.changes

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ChangeListListener
import com.intellij.openapi.vcs.changes.ChangeListManager
import zielu.gittoolbox.util.LocalGateway

internal class ChangeListSubscriberLocalGateway(private val project: Project): LocalGateway(project) {

  fun subscribe(listener: ChangeListListener) {
    ChangeListManager.getInstance(project).addChangeListListener(listener, project)
  }

  fun changeListChanged(id: String, changes: Collection<Change>) {
    runInBackground { handleChangeListChanged(id, changes) }
  }

  private fun handleChangeListChanged(id: String, changes: Collection<Change>) {
    ChangesTrackerService.getInstance(project).changeListChanged(id, changes)
  }
}
