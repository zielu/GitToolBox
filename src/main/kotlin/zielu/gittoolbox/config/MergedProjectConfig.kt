package zielu.gittoolbox.config

import zielu.gittoolbox.config.override.BoolValueOverride
import zielu.gittoolbox.formatter.Formatter
import kotlin.reflect.KProperty0

internal class MergedProjectConfig(
  private val appConfig: GitToolBoxConfig2,
  private val projectConfig: GitToolBoxConfigPrj,
  private val useLegacy: Boolean
) {

  fun autoFetchEnabled(): Boolean {
    return getBool(
      projectConfig::autoFetch,
      appConfig::autoFetchEnabled,
      projectConfig.autoFetchEnabledOverride
    )
  }

  private fun getBool(legacy: KProperty0<Boolean>, app: KProperty0<Boolean>, override: BoolValueOverride): Boolean {
    return when {
      useLegacy -> {
        legacy.get()
      }
      override.enabled -> {
        override.value
      }
      else -> {
        app.get()
      }
    }
  }

  fun autoFetchIntervalMinutes(): Int {
    return when {
      useLegacy -> {
        projectConfig.autoFetchIntervalMinutes
      }
      projectConfig.autoFetchIntervalMinutesOverride.enabled -> {
        projectConfig.autoFetchIntervalMinutesOverride.value
      }
      else -> {
        appConfig.autoFetchIntervalMinutes
      }
    }
  }

  fun autoFetchOnBranchSwitch(): Boolean {
    return getBool(
      projectConfig::autoFetchOnBranchSwitch,
      appConfig::autoFetchOnBranchSwitch,
      projectConfig.autoFetchOnBranchSwitchOverride
    )
  }

  fun commitDialogBranchCompletion(): Boolean {
    return getBool(
      projectConfig::commitDialogCompletion,
      appConfig::commitDialogCompletion,
      projectConfig.commitDialogBranchCompletionOverride
    )
  }

  fun commitDialogGitmojiCompletion(): Boolean {
    return getBool(
      appConfig::commitDialogGitmojiCompletion,
      appConfig::commitDialogGitmojiCompletion,
      projectConfig.commitDialogGitmojiCompletionOverride
    )
  }

  fun commitDialogCompletionFormatters(): List<Formatter> {
    return when {
      useLegacy -> {
        projectConfig.completionConfigs.map { it.getFormatter() }
      }
      projectConfig.completionConfigsOverride.enabled -> {
        projectConfig.completionConfigsOverride.values.map { it.getFormatter() }
      }
      else -> {
        appConfig.completionConfigs.map { it.getFormatter() }
      }
    }
  }

  fun referencePointForStatus(): ReferencePointForStatusConfig {
    return when {
      useLegacy -> {
        projectConfig.referencePointForStatus
      }
      projectConfig.referencePointForStatusOverride.enabled -> {
        projectConfig.referencePointForStatusOverride.value
      }
      else -> {
        appConfig.referencePointForStatus
      }
    }
  }

  fun commitMessageValidation(): Boolean {
    return getBool(
      projectConfig::commitMessageValidation,
      appConfig::commitMessageValidationEnabled,
      projectConfig.commitMessageValidationOverride
    )
  }

  fun setCommitMessageValidation(value: Boolean) {
    return when {
      useLegacy -> {
        projectConfig.commitMessageValidation = value
      }
      projectConfig.commitMessageValidationOverride.enabled -> {
        projectConfig.commitMessageValidationOverride.value = value
      }
      else -> {
        appConfig.commitMessageValidationEnabled = value
      }
    }
  }

  fun commitMessageValidationRegex(): String {
    return when {
      useLegacy -> {
        projectConfig.commitMessageValidationRegex
      }
      projectConfig.commitMessageValidationRegexOverride.enabled -> {
        projectConfig.commitMessageValidationRegexOverride.value
      }
      else -> {
        appConfig.commitMessageValidationRegex
      }
    }
  }

  fun isReferencePointForStatusChanged(previous: MergedProjectConfig): Boolean {
    return previous.referencePointForStatus() != referencePointForStatus()
  }
}
