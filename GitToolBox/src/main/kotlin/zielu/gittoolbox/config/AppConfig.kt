package zielu.gittoolbox.config

import zielu.gittoolbox.util.AppUtil

internal object AppConfig {
  @JvmStatic
  fun get(): GitToolBoxConfig2 {
    return getConfig()
  }

  private fun getConfig(): GitToolBoxConfig2 {
    return AppUtil.getServiceInstance(GitToolBoxConfig2::class.java)
  }
}
