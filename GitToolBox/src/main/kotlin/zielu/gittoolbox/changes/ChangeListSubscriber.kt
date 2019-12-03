package zielu.gittoolbox.changes

import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.ChangeList
import com.intellij.openapi.vcs.changes.ChangeListListener
import com.intellij.openapi.vcs.changes.LocalChangeList

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
  }

  fun handleChangeListRemoved(id: String) {
    log.debug("Change list removed", id)
    gateway.changeListRemoved(id)
  }

  fun handleChangeListsChanged() {
    log.debug("Change lists changed")
    gateway.changeListsChanged()
  }

  private companion object {
    private val log = Logger.getInstance(ChangeListSubscriber::class.java)
  }
}
