package zielu.gittoolbox.config

import zielu.gittoolbox.metrics.AppMetrics
import zielu.gittoolbox.util.AppUtil
import java.util.concurrent.atomic.AtomicBoolean

internal object AppConfig {
  private val migrationDone = AtomicBoolean()

  @JvmStatic
  fun get(): GitToolBoxConfig2 {
    if (migrationDone.get()) {
      return getConfig()
    } else {
      synchronized(this) {
        val config = getConfig()
        if (migrationDone.get()) {
          return config
        } else {
          val timer = AppMetrics.getInstance().timer("app-config.migrate")
          val migratedConfig = timer.timeSupplier { migrate(config) }
          migrationDone.set(true)
          return migratedConfig
        }
      }
    }
  }

  private fun getConfig(): GitToolBoxConfig2 {
    return AppUtil.getServiceInstance(GitToolBoxConfig2::class.java)
  }

  private fun migrate(config: GitToolBoxConfig2): GitToolBoxConfig2 {
    if (ConfigMigrator().migrate(config)) {
      AppUtil.saveAppSettings()
    }
    return config
  }
}
