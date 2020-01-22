package zielu.gittoolbox.startup

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.util.ThrowableRunnable
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
      saveAppSettings()
    }
  }

  private fun saveAppSettings() {
    val application = ApplicationManager.getApplication()
    if (!application.isUnitTestMode) {
      log.info("Saving settings")
      try {
        WriteAction.runAndWait(ThrowableRunnable<Exception> { application.saveSettings() })
      } catch (exception: Exception) {
        log.error("Failed to save settings", exception)
      }
    }
  }

  companion object {
    private val log = Logger.getInstance(GitToolBoxStartup::class.java)
  }
}
