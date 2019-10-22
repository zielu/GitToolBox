package zielu.gittoolbox.startup

import zielu.gittoolbox.config.AutoFetchExclusionConfig
import zielu.gittoolbox.config.GitToolBoxConfigPrj

class ConfigForProjectMigrator(private val config: GitToolBoxConfigPrj) {
  fun migrate(): Boolean {
    var migrated = false
    if (config.autoFetchExclusions.isNotEmpty()) {
      val configs = ArrayList(config.autoFetchExclusionConfigs)
      configs.addAll(config.autoFetchExclusions.map { AutoFetchExclusionConfig(it) })
      config.autoFetchExclusionConfigs = configs
      config.autoFetchExclusions = ArrayList()
      migrated = true
    }
    return migrated
  }
}
