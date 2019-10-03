package zielu.gittoolbox.startup

import zielu.gittoolbox.config.AutoFetchExclusionConfig
import zielu.gittoolbox.config.GitToolBoxConfigForProject

class ConfigForProjectMigrator(private val config: GitToolBoxConfigForProject) {
  fun migrate(): Boolean {
    config.autoFetchExclusionConfigs.let {
      config.autoFetchExclusionConfigs = config.autoFetchExclusions.map { AutoFetchExclusionConfig(it) }
      return true
    }
  }
}
