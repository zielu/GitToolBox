package zielu.gittoolbox.config

import com.intellij.openapi.project.Project
import zielu.gittoolbox.metrics.ProjectMetrics
import zielu.gittoolbox.util.AppUtil
import java.util.concurrent.atomic.AtomicBoolean

internal class ProjectConfig {
  private val migrationDone = AtomicBoolean()

  companion object {
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
        if (migrationDone.get()) {
          return getConfig(project)
        } else {
          val config = getConfig(project)
          val appConfig = AppConfig.get()
          val timer = ProjectMetrics.getInstance(project).timer("project-config.migrate")
          val migratedConfig = timer.timeSupplier { migrate(project, config, appConfig) }
          migrationDone.set(true)
          return migratedConfig
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
  ): GitToolBoxConfigPrj {
    if (ConfigMigrator().migrate(project, config, appConfig)) {
      AppUtil.saveAppSettings()
    }
    return config
  }
}
