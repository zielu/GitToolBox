package zielu.gittoolbox.config

internal open class MergedProjectConfig(
  private val appConfig: GitToolBoxConfig2,
  private val projectConfig: GitToolBoxConfigPrj
) {

  open fun autoFetchEnabled(): Boolean {
    return if (projectConfig.autoFetchEnabledOverride.enabled) {
      projectConfig.autoFetchEnabledOverride.value
    } else {
      appConfig.autoFetchEnabled
    }
  }

  open fun autoFetchIntervalMinutes(): Int {
    return if (projectConfig.autoFetchIntervalMinutesOverride.enabled) {
      projectConfig.autoFetchIntervalMinutesOverride.value
    } else {
      return appConfig.autoFetchIntervalMinutes
    }
  }

  open fun autoFetchOnBranchSwitch(): Boolean {
    return if (projectConfig.autoFetchOnBranchSwitchOverride.enabled) {
      projectConfig.autoFetchOnBranchSwitchOverride.value
    } else {
      return appConfig.autoFetchOnBranchSwitch
    }
  }

  fun autoFetchExclusionConfigs(): List<AutoFetchExclusionConfig> {
    return projectConfig.autoFetchExclusionConfigs
  }
}
