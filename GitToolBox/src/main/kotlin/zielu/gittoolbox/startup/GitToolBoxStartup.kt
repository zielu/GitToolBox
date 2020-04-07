package zielu.gittoolbox.startup

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity

internal class GitToolBoxStartup : StartupActivity {
  override fun runActivity(project: Project) {
    if (!project.isDefault) {
      GitToolBoxStartupGateway(project).fireProjectReady()
    }
  }
}
