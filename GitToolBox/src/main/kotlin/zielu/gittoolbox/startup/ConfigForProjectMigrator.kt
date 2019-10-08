package zielu.gittoolbox.startup

import zielu.gittoolbox.config.AutoFetchExclusionConfig
import zielu.gittoolbox.config.GitToolBoxConfigPrj

class ConfigForProjectMigrator(private val config: GitToolBoxConfigPrj) {
  fun migrate(): Boolean {
    var migrated = false
    if (config.version < 2) {
      config.autoFetchExclusionConfigs = config.autoFetchExclusions.map { AutoFetchExclusionConfig(it) }
      config.version = 2
      migrated = true
    }
    return migrated
  }
}
