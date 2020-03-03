package zielu.gittoolbox.startup

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import zielu.gittoolbox.metrics.ProjectMetrics

internal class GitToolBoxStartup : StartupActivity {
  override fun runActivity(project: Project) {
    ProjectMetrics.getInstance(project).timer("startup.migrate").time { migrate(project) }
    if (!project.isDefault) {
      GitToolBoxStartupGateway(project).fireProjectReady()
    }
  }

  private fun migrate(project: Project) {
    if (ConfigMigrator().migrate(project)) {
      GitToolBoxStartupGateway(project).saveSettings()
    }
  }
}
