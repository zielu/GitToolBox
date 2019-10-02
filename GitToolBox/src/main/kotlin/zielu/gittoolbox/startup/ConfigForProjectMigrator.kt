package zielu.gittoolbox.startup

import zielu.gittoolbox.config.AutoFetchExclusionConfig
import zielu.gittoolbox.config.GitToolBoxConfigForProject

class ConfigForProjectMigrator(private val config: GitToolBoxConfigForProject) {
    fun migrate(): Boolean {
        if (config.autoFetchExclusionConfigs == null) {
            config.autoFetchExclusionConfigs = config.autoFetchExclusions.map { AutoFetchExclusionConfig(it) }
            return true
        }
        return false
    }
}