package zielu.gittoolbox.startup

import com.intellij.openapi.project.Project
import zielu.gittoolbox.lifecycle.ProjectLifecycleNotifier
import zielu.gittoolbox.util.BaseFacade

internal class GitToolBoxStartupFacade(private val project: Project) : BaseFacade(project) {
  fun fireProjectReady() {
    publishSync { it.syncPublisher(ProjectLifecycleNotifier.TOPIC).projectReady(project) }
  }
}
