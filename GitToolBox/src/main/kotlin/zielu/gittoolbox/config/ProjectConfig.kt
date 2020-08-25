package zielu.gittoolbox.config

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import zielu.gittoolbox.metrics.ProjectMetrics
import zielu.gittoolbox.util.AppUtil
import java.util.concurrent.atomic.AtomicBoolean

internal class ProjectConfig {
  private val migrationDone = AtomicBoolean()

  companion object {
    private val log = Logger.getInstance(ProjectConfig::class.java)

    @JvmStatic
    fun get(project: Project): GitToolBoxConfigPrj {
      return AppUtil.getServiceInstance(project, ProjectConfig::class.java).getInternal(project)
    }
  }

  private fun getInternal(project: Project): GitToolBoxConfigPrj {
    if (migrationDone.get()) {
      return getConfig(project)
    } else {
      synchronized(project) {
        return if (migrationDone.get()) {
          getConfig(project)
        } else {
          val config = getConfig(project)
          val appConfig = AppConfig.get()
          val timer = ProjectMetrics.getInstance(project).timer("project-config.migrate")
          val result = timer.timeSupplier { migrate(project, config, appConfig) }
          migrationDone.set(true)
          if (result.migrated) {
            log.info("Migration done")
          } else {
            log.info("Already migrated")
          }
          result.config
        }
      }
    }
  }

  private fun getConfig(project: Project): GitToolBoxConfigPrj {
    return AppUtil.getServiceInstance(project, GitToolBoxConfigPrj::class.java)
  }

  private fun migrate(
    project: Project,
    config: GitToolBoxConfigPrj,
    appConfig: GitToolBoxConfig2
  ): ProjectMigrationResult {
    val migrated = ConfigMigrator().migrate(project, config, appConfig)
    return ProjectMigrationResult(migrated, config)
  }
}

private data class ProjectMigrationResult(
  val migrated: Boolean,
  val config: GitToolBoxConfigPrj
)
