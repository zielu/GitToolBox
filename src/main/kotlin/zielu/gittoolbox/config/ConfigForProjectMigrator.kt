package zielu.gittoolbox.config

internal class ConfigForProjectMigrator(private val config: GitToolBoxConfigPrj) {
  fun migrate(): Boolean {
    var migrated = false
    migrated = migrateAutoFetchExclusions() || migrated
    return migrated
  }

  private fun migrateAutoFetchExclusions(): Boolean {
    var migrated = false
    if (config.autoFetchExclusions.isNotEmpty()) {
      val configs = config.autoFetchExclusionConfigs.groupBy { autoFetchExclusionConfig: AutoFetchExclusionConfig ->
        autoFetchExclusionConfig.repositoryRootPath
      }
      val cleanConfigs = LinkedHashMap<String, AutoFetchExclusionConfig>()
      configs.entries.forEach { exConfigs ->
        if (exConfigs.value.size > 1) {
          exConfigs.value.forEach { exConfig ->
            if (exConfig.hasRemotes()) {
              cleanConfigs[exConfigs.key] = exConfig
            }
          }
          migrated = true
        } else {
          cleanConfigs[exConfigs.key] = exConfigs.value[0]
        }
      }

      config.autoFetchExclusions.forEach { excludedRepo ->
        if (!cleanConfigs.containsKey(excludedRepo)) {
          cleanConfigs[excludedRepo] = AutoFetchExclusionConfig(excludedRepo)
          migrated = true
        }
      }

      if (migrated) {
        config.autoFetchExclusionConfigs = ArrayList(cleanConfigs.values)
      }
    }
    return migrated
  }
}
