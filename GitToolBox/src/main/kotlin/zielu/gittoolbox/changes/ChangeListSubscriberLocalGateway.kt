package zielu.gittoolbox.changes

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.ChangeListListener
import com.intellij.openapi.vcs.changes.ChangeListManager
import zielu.gittoolbox.config.ConfigNotifier
import zielu.gittoolbox.config.GitToolBoxConfig2
import zielu.gittoolbox.util.LocalGateway

internal class ChangeListSubscriberLocalGateway(private val project: Project) : LocalGateway(project) {

  fun subscribe(listener: ChangeListListener) {
    ChangeListManager.getInstance(project).addChangeListListener(listener, project)
  }

  fun subscribe(listener: ConfigNotifier) {
    project.messageBus.connect(project).subscribe(ConfigNotifier.CONFIG_TOPIC, listener)
  }

  fun changeListRemoved(id: String) {
    runInBackground { ChangesTrackerService.getInstance(project).changeListRemoved(id) }
  }

  fun changeListsChanged(changeListsData: Collection<ChangeListData>) {
    runInBackground { changeListsData.forEach { handleChangeListChanged(it) } }
  }

  private fun handleChangeListChanged(changeListData: ChangeListData) {
    ChangesTrackerService.getInstance(project).changeListChanged(changeListData)
  }

  fun getAllChangeLists(): Collection<ChangeListData> {
    return ApplicationManager.getApplication().runReadAction<Collection<ChangeListData>> { getAllChangeListsData() }
  }

  private fun getAllChangeListsData(): Collection<ChangeListData> {
    val changeLists = ChangeListManager.getInstance(project).changeLists
    return changeLists.map { ChangeListData(it) }
  }

  fun getEnabled() = GitToolBoxConfig2.getInstance().trackChanges
}
