package zielu.gittoolbox.changes

import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.ChangeList
import com.intellij.openapi.vcs.changes.ChangeListListener
import com.intellij.openapi.vcs.changes.LocalChangeList
import zielu.gittoolbox.config.ConfigNotifier
import zielu.gittoolbox.config.GitToolBoxConfig2

internal class ChangeListSubscriber(project: Project) : ProjectComponent {
  private val gateway = ChangeListSubscriberLocalGateway(project)

  override fun projectOpened() {
    gateway.subscribe(object : ChangeListListener {
      override fun changeListRemoved(list: ChangeList) {
        if (list is LocalChangeList) {
          handleChangeListRemoved(list.id)
        }
      }

      override fun changeListUpdateDone() {
        handleChangeListsChanged()
      }
    })

    gateway.subscribe(object : ConfigNotifier {
      override fun configChanged(previous: GitToolBoxConfig2, current: GitToolBoxConfig2) {
        handleChangeListsChanged()
      }
    })
  }

  fun handleChangeListRemoved(id: String) {
    log.debug("Change list removed", id)
    if (gateway.getTrackingEnabled()) {
      gateway.changeListRemoved(id)
    }
  }

  fun handleChangeListsChanged() {
    log.debug("Change lists changed")
    if (gateway.getTrackingEnabled()) {
      gateway.changeListsChanged(gateway.getAllChangeLists())
    }
  }

  private companion object {
    private val log = Logger.getInstance(ChangeListSubscriber::class.java)
  }
}
