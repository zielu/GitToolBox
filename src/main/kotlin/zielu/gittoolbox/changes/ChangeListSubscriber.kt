package zielu.gittoolbox.changes

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.ChangeList
import com.intellij.openapi.vcs.changes.LocalChangeList
import zielu.gittoolbox.config.GitToolBoxConfig2
import zielu.gittoolbox.util.AppUtil

internal class ChangeListSubscriber(project: Project) : Disposable {
  private val facade = ChangeListSubscriberFacade(project)
  init {
    facade.registerDisposable(this, facade)
  }

  fun onChangeListsUpdated() {
    log.debug("Change lists changed")
    if (facade.getTrackingEnabled()) {
      facade.changeListsChanged(facade.getAllChangeListsData())
    }
  }

  fun onChangeListRemoved(changeList: ChangeList) {
    if (changeList is LocalChangeList) {
      onChangeListRemoved(changeList.id)
    }
  }

  private fun onChangeListRemoved(id: String) {
    log.debug("Change list removed", id)
    if (facade.getTrackingEnabled()) {
      facade.changeListRemoved(id)
    }
  }

  fun onConfigChanged(current: GitToolBoxConfig2) {
    if (current.isChangesTrackingEnabled()) {
      onChangeListsUpdated()
    }
  }

  override fun dispose() {
    // do nothing
  }

  companion object {
    private val log = Logger.getInstance(ChangeListSubscriber::class.java)

    fun getInstance(project: Project): ChangeListSubscriber {
      return AppUtil.getServiceInstance(project, ChangeListSubscriber::class.java)
    }
  }
}
