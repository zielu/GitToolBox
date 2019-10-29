package zielu.gittoolbox.fetch

import com.intellij.openapi.project.Project
import zielu.gittoolbox.extension.AutoFetchAllowed
import zielu.gittoolbox.util.LocalGateway

internal class AutoFetchAllowedLocalGateway(project: Project) : LocalGateway(project) {
  private val messageBus by lazy {
    project.messageBus
  }

  fun fireStateChanged(autoFetchAllowed: AutoFetchAllowed) {
    runInBackground { messageBus.syncPublisher(AutoFetchAllowed.TOPIC).stateChanged(autoFetchAllowed) }
  }
}
