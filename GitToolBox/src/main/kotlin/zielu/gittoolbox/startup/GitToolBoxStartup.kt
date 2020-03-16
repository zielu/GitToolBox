package zielu.gittoolbox.startup

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import zielu.gittoolbox.metrics.ProjectMetrics
import zielu.intellij.log.info

internal class GitToolBoxStartup : StartupActivity {
  override fun runActivity(project: Project) {
    ProjectMetrics.getInstance(project).timer("startup.migrate").time { migrate(project) }
    if (!project.isDefault) {
      GitToolBoxStartupGateway(project).fireProjectReady()
    }
  }

  private fun migrate(project: Project) {
    if (ConfigMigrator().migrate(project)) {
      log.info("Project migrated ", project)
      GitToolBoxStartupGateway(project).saveSettings()
    }
  }

  private companion object {
    private val log: Logger = Logger.getInstance(GitToolBoxStartup::class.java)
  }
}
