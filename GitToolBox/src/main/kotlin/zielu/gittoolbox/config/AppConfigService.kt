package zielu.gittoolbox.config

import zielu.gittoolbox.util.AppUtil
import java.util.concurrent.atomic.AtomicBoolean

internal object AppConfig {
  private val migrated = AtomicBoolean()

  @JvmStatic
  fun getAppConfig(): GitToolBoxConfig2 {
    if (migrated.get()) {
      return getConfig()
    } else {
      synchronized(this) {
        val config = getConfig()
        if (migrated.get()) {
          return config
        } else {
          return config
        }
      }
    }
  }

  private fun getConfig(): GitToolBoxConfig2 {
    return AppUtil.getServiceInstance(GitToolBoxConfig2::class.java)
  }
}
