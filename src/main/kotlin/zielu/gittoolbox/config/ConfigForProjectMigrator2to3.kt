package zielu.gittoolbox.config

import zielu.gittoolbox.config.override.BoolValueOverride
import zielu.gittoolbox.config.override.IntValueOverride
import kotlin.reflect.KProperty0

internal class ConfigForProjectMigrator2to3(
  private val appConfig: GitToolBoxConfig2,
  private val config: GitToolBoxConfigPrj
) {
  fun migrate(): Boolean {
    val migrated = mutableListOf<Boolean>()
    migrated.add(
      applyBoolOverride(
        config::autoFetch,
        appConfig::autoFetchEnabled,
        config.autoFetchEnabledOverride
      )
    )
    migrated.add(
      applyIntOverride(
        config::autoFetchIntervalMinutes,
        appConfig::autoFetchIntervalMinutes,
        config.autoFetchIntervalMinutesOverride
      )
    )
    migrated.add(
      applyBoolOverride(
        config::autoFetchOnBranchSwitch,
        appConfig::autoFetchOnBranchSwitch,
        config.autoFetchOnBranchSwitchOverride
      )
    )
    migrated.add(
      applyBoolOverride(
        config::commitDialogCompletion,
        appConfig::commitDialogCompletion,
        config.commitDialogBranchCompletionOverride
      )
    )
    if (config.completionConfigs != appConfig.completionConfigs) {
      config.completionConfigsOverride.enabled = true
      config.completionConfigsOverride.values = config.completionConfigs.map { it.copy() }
      migrated.add(true)
    }
    if (config.referencePointForStatus != appConfig.referencePointForStatus) {
      config.referencePointForStatusOverride.enabled = true
      config.referencePointForStatusOverride.value = config.referencePointForStatus.copy()
      migrated.add(true)
    }
    migrated.add(
        applyBoolOverride(
        config::commitMessageValidation,
        appConfig::commitMessageValidationEnabled,
        config.commitMessageValidationOverride
      )
    )

    return migrated.any { it }
  }

  private fun applyBoolOverride(
    prjProp: KProperty0<Boolean>,
    appProp: KProperty0<Boolean>,
    override: BoolValueOverride
  ): Boolean {
    if (prjProp.get() != appProp.get()) {
      override.enabled = true
      override.value = prjProp.get()
      return true
    }
    return false
  }

  private fun applyIntOverride(
    prjProp: KProperty0<Int>,
    appProp: KProperty0<Int>,
    override: IntValueOverride
  ): Boolean {
    if (prjProp.get() != appProp.get()) {
      override.enabled = true
      override.value = prjProp.get()
      return true
    }
    return false
  }
}
