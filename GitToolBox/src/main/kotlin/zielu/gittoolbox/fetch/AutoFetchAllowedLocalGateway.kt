package zielu.gittoolbox.fetch

import com.intellij.openapi.project.Project
import zielu.gittoolbox.extension.autofetch.AUTO_FETCH_ALLOWED_TOPIC
import zielu.gittoolbox.extension.autofetch.AutoFetchAllowed
import zielu.gittoolbox.util.LocalGateway

internal class AutoFetchAllowedLocalGateway(project: Project) : LocalGateway(project) {
  private val messageBus by lazy {
    project.messageBus
  }

  fun fireStateChanged(autoFetchAllowed: AutoFetchAllowed) {
    runInBackground { messageBus.syncPublisher(AUTO_FETCH_ALLOWED_TOPIC).stateChanged(autoFetchAllowed) }
  }
}
