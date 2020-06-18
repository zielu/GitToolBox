package zielu.gittoolbox.config

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import zielu.gittoolbox.metrics.AppMetrics
import zielu.gittoolbox.util.AppUtil
import java.util.concurrent.atomic.AtomicBoolean

internal object AppConfig {
  private val log = Logger.getInstance(AppConfig::class.java)
  private val migrationDone = AtomicBoolean()

  @JvmStatic
  fun get(): GitToolBoxConfig2 {
    if (migrationDone.get()) {
      return getConfig()
    } else {
      synchronized(this) {
        val config = getConfig()
        return if (migrationDone.get()) {
          config
        } else {
          val timer = AppMetrics.getInstance().timer("app-config.migrate")
          val result = timer.timeSupplier { migrate(config) }
          migrationDone.set(true)
          if (result.migrated) {
            saveSettings()
          }
          return result.config
        }
      }
    }
  }

  private fun getConfig(): GitToolBoxConfig2 {
    return AppUtil.getServiceInstance(GitToolBoxConfig2::class.java)
  }

  private fun saveSettings() {
    ApplicationManager.getApplication().executeOnPooledThread {
      AppUtil.saveAppSettings()
      log.info("Settings saved after project config migration")
    }
  }

  private fun migrate(config: GitToolBoxConfig2): AppMigrationResult {
    val migrated = ConfigMigrator().migrate(config)
    return AppMigrationResult(migrated, config)
  }
}

private data class AppMigrationResult(
  val migrated: Boolean,
  val config: GitToolBoxConfig2
)
