package zielu.gittoolbox.changes

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ChangeListListener
import com.intellij.openapi.vcs.changes.ChangeListManager
import zielu.gittoolbox.util.LocalGateway

internal class ChangeListSubscriberLocalGateway(private val project: Project): LocalGateway(project) {

  fun subscribe(listener: ChangeListListener) {
    ChangeListManager.getInstance(project).addChangeListListener(listener, project)
  }

  fun changeListRemoved(id: String) {
    runInBackground { ChangesTrackerService.getInstance(project).changeListRemoved(id) }
  }

  fun changeListsChanged() {
    val allChangeLists = ApplicationManager.getApplication()
      .runReadAction<Collection<ChangeListData>> { getAllChangeLists() }
    runInBackground { allChangeLists.forEach { handleChangeListChanged(it.id, it.changes) } }
  }

  private fun handleChangeListChanged(id: String, changes: Collection<Change>) {
    ChangesTrackerService.getInstance(project).changeListChanged(id, changes)
  }

  private fun getAllChangeLists(): Collection<ChangeListData> {
    return ChangeListManager.getInstance(project).changeLists.map { ChangeListData(it.id, ArrayList(it.changes)) }
  }

  private data class ChangeListData(
    val id: String,
    val changes: Collection<Change>
  )
}
