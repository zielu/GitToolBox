package zielu.gittoolbox.changes

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.ChangeList
import com.intellij.openapi.vcs.changes.ChangeListListener
import com.intellij.openapi.vcs.changes.LocalChangeList
import zielu.gittoolbox.config.GitToolBoxConfig2
import zielu.gittoolbox.util.AppUtil

internal class ChangeListSubscriber(project: Project) {
  private val gateway = ChangeListSubscriberLocalGateway(project)

  fun onProjectOpened() {
    gateway.subscribe(object : ChangeListListener {
      override fun changeListRemoved(list: ChangeList) {
        if (list is LocalChangeList) {
          onChangeListRemoved(list.id)
        }
      }

      override fun changeListUpdateDone() {
        onChangeListsUpdated()
      }
    })
  }

  fun onChangeListsUpdated() {
    log.debug("Change lists changed")
    if (gateway.getTrackingEnabled()) {
      gateway.changeListsChanged(gateway.getAllChangeListsData())
    }
  }

  fun onChangeListRemoved(id: String) {
    log.debug("Change list removed", id)
    if (gateway.getTrackingEnabled()) {
      gateway.changeListRemoved(id)
    }
  }

  fun onConfigChanged(current: GitToolBoxConfig2) {
    if (current.isChangesTrackingEnabled) {
      onChangeListsUpdated()
    }
  }

  companion object {
    private val log = Logger.getInstance(ChangeListSubscriber::class.java)

    fun getInstance(project: Project): ChangeListSubscriber {
      return AppUtil.getServiceInstance(project, ChangeListSubscriber::class.java)
    }
  }
}
