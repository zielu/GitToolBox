package zielu.gittoolbox.changes

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.ChangeListManager
import zielu.gittoolbox.config.AppConfig
import zielu.gittoolbox.util.AppUtil
import zielu.gittoolbox.util.LocalGateway
import zielu.intellij.util.ZDisposeGuard

internal class ChangeListSubscriberLocalGateway(
  private val project: Project
) : Disposable, LocalGateway(project) {
  private val disposeGuard = ZDisposeGuard()

  fun changeListRemoved(id: String) {
    runInBackground(this) { ChangesTrackerService.getInstance(project).changeListRemoved(id) }
  }

  fun changeListsChanged(changeListsData: Collection<ChangeListData>) {
    runInBackground(this) { changeListsData.forEach { handleChangeListChanged(it) } }
  }

  private fun handleChangeListChanged(changeListData: ChangeListData) {
    if (disposeGuard.isActive()) {
      ChangesTrackerService.getInstance(project).changeListChanged(changeListData)
    }
  }

  fun getAllChangeListsData(): Collection<ChangeListData> {
    return if (disposeGuard.isActive()) {
      AppUtil.runReadAction { getAllChangeListsDataInternal() }
    } else {
      listOf()
    }
  }

  private fun getAllChangeListsDataInternal(): Collection<ChangeListData> {
    val changeLists = ChangeListManager.getInstance(project).changeLists
    return changeLists.map { ChangeListData(it) }
  }

  fun getTrackingEnabled() = AppConfig.getConfig().isChangesTrackingEnabled()

  override fun dispose() {
    dispose(disposeGuard)
  }
}
