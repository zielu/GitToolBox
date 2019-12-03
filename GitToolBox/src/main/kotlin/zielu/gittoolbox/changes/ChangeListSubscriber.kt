package zielu.gittoolbox.changes

import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ChangeList
import com.intellij.openapi.vcs.changes.ChangeListListener
import com.intellij.openapi.vcs.changes.LocalChangeList

internal class ChangeListSubscriber(project: Project) : ProjectComponent {
  private val gateway = ChangeListSubscriberLocalGateway(project)


  override fun projectOpened() {
    gateway.subscribe(object : ChangeListListener {
      override fun changeListChanged(list: ChangeList) {
        if (list is LocalChangeList) {
          handleChangeListChanged(list.id, ArrayList(list.changes))
        }
      }
    })
  }

  fun handleChangeListChanged(id: String, changes: Collection<Change>) {
    log.debug("Change list changed", id)
    gateway.changeListChanged(id, changes)
  }

  private companion object {
    private val log = Logger.getInstance(ChangeListSubscriber::class.java)
  }
}
