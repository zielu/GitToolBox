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
    return getBoolWithLegacy(
      projectConfig::autoFetch,
      appConfig::autoFetchEnabled,
      projectConfig.autoFetchEnabledOverride
    )
  }

  private fun getBoolWithLegacy(
    legacy: KProperty0<Boolean>,
    app: KProperty0<Boolean>,
    override: BoolValueOverride
  ): Boolean {
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
    return getBoolWithLegacy(
      projectConfig::autoFetchOnBranchSwitch,
      appConfig::autoFetchOnBranchSwitch,
      projectConfig.autoFetchOnBranchSwitchOverride
    )
  }

  fun commitDialogBranchCompletion(): Boolean {
    return getBoolWithLegacy(
      projectConfig::commitDialogCompletion,
      appConfig::commitDialogCompletion,
      projectConfig.commitDialogBranchCompletionOverride
    )
  }

  fun commitDialogGitmojiCompletion(): Boolean {
    return getBoolWithLegacy(
      appConfig::commitDialogGitmojiCompletion,
      appConfig::commitDialogGitmojiCompletion,
      projectConfig.commitDialogGitmojiCompletionOverride
    )
  }

  fun commitDialogGitmojiUnicodeCompletion(): Boolean {
    return getBoolWithLegacy(
      appConfig::commitDialogGitmojiCompletionUnicode,
      appConfig::commitDialogGitmojiCompletionUnicode,
      projectConfig.commitDialogGitmojiUnicodeCompletionOverride
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
    return getBoolWithLegacy(
      projectConfig::commitMessageValidation,
      appConfig::commitMessageValidationEnabled,
      projectConfig.commitMessageValidationOverride
    )
  }

  fun setCommitMessageValidation(value: Boolean) {
    if (useLegacy) {
      projectConfig.commitMessageValidation = value
    } else {
      if (value != commitMessageValidation()) {
        if (value == appConfig.commitMessageValidationEnabled) {
          projectConfig.commitMessageValidationOverride.enabled = false
        } else {
          projectConfig.commitMessageValidationOverride.enabled = true
          projectConfig.commitMessageValidationOverride.value = value
        }
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

  fun outdatedBranchesAutoCleanupEnabled(): Boolean {
    return when {
      projectConfig.outdatedBranchesCleanupOverride.enabled -> {
        projectConfig.outdatedBranchesCleanupOverride.value.autoCheckEnabled
      }
      else -> {
        appConfig.outdatedBranchesCleanup.autoCheckEnabled
      }
    }
  }

  fun outdatedBranchesAutoCleanupIntervalHours(): Int {
    return when {
      projectConfig.outdatedBranchesCleanupOverride.enabled -> {
        projectConfig.outdatedBranchesCleanupOverride.value.autoCheckIntervalHours
      }
      else -> {
        appConfig.outdatedBranchesCleanup.autoCheckIntervalHours
      }
    }
  }

  fun outdatedBranchesCleanupExclusionGlobs(): List<String> {
    return when {
      projectConfig.outdatedBranchesCleanupOverride.enabled -> {
        projectConfig.outdatedBranchesCleanupOverride.value.exclusionGlobs
      }
      else -> {
        appConfig.outdatedBranchesCleanup.exclusionGlobs
      }
    }
  }
}
