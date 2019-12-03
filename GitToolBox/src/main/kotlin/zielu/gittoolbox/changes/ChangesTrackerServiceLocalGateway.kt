package zielu.gittoolbox.changes

import com.intellij.openapi.project.Project
import zielu.gittoolbox.util.LocalGateway

internal class ChangesTrackerServiceLocalGateway(project: Project): LocalGateway(project) {
  private val messageBus by lazy {
    project.messageBus
  }

  fun fireChangesCountUpdated(changesCount: Int) {
    messageBus.syncPublisher(CHANGES_TRACKER_TOPIC).changesCountChanged(changesCount)
  }
}
