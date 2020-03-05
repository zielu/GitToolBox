package zielu.gittoolbox.startup

import com.intellij.openapi.project.Project
import zielu.gittoolbox.lifecycle.ProjectLifecycleNotifier
import zielu.gittoolbox.util.AppUtil
import zielu.gittoolbox.util.LocalGateway

internal class GitToolBoxStartupGateway(private val project: Project) : LocalGateway(project) {
  fun fireProjectReady() {
    runInBackground {
      project.messageBus.syncPublisher(ProjectLifecycleNotifier.TOPIC).projectReady(project)
    }
  }

  fun saveSettings() {
    AppUtil.saveAppSettings()
  }
}
