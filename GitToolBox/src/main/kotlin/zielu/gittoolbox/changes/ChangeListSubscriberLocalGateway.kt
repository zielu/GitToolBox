package zielu.gittoolbox.changes

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.ChangeListManager
import zielu.gittoolbox.config.AppConfig
import zielu.gittoolbox.util.AppUtil
import zielu.gittoolbox.util.LocalGateway

internal class ChangeListSubscriberLocalGateway(private val project: Project) : LocalGateway(project) {
  fun changeListRemoved(id: String) {
    runInBackground { ChangesTrackerService.getInstance(project).changeListRemoved(id) }
  }

  fun changeListsChanged(changeListsData: Collection<ChangeListData>) {
    runInBackground { changeListsData.forEach { handleChangeListChanged(it) } }
  }

  private fun handleChangeListChanged(changeListData: ChangeListData) {
    ChangesTrackerService.getInstance(project).changeListChanged(changeListData)
  }

  fun getAllChangeListsData(): Collection<ChangeListData> {
    return AppUtil.runReadAction { getAllChangeListsDataInternal() }
  }

  private fun getAllChangeListsDataInternal(): Collection<ChangeListData> {
    val changeLists = ChangeListManager.getInstance(project).changeLists
    return changeLists.map { ChangeListData(it) }
  }

  fun getTrackingEnabled() = AppConfig.getConfig().isChangesTrackingEnabled()
}
