package zielu.gittoolbox.config

import com.intellij.util.xmlb.annotations.Transient
import zielu.gittoolbox.branch.OutdatedBranchCleanupParams
import zielu.gittoolbox.config.override.BoolValueOverride
import zielu.gittoolbox.config.override.CommitCompletionConfigListOverride
import zielu.gittoolbox.config.override.IntValueOverride
import zielu.gittoolbox.config.override.ListValueOverride
import zielu.gittoolbox.config.override.OutdatedBranchesAutoCleanupOverride
import zielu.gittoolbox.config.override.ReferencePointForStatusOverride
import zielu.gittoolbox.config.override.StringValueOverride
import zielu.gittoolbox.fetch.AutoFetchParams

internal data class GitToolBoxConfigPrj(
  @Deprecated("Since 201.4.0") var autoFetch: Boolean = true,
  @Deprecated("Since 201.4.0") var autoFetchIntervalMinutes: Int = AutoFetchParams.DEFAULT_INTERVAL_MINUTES,
  @Deprecated("Since 192.3.1") var autoFetchExclusions: List<String> = arrayListOf(),
  @Deprecated("Since 201.4.0") var autoFetchOnBranchSwitch: Boolean = true,
  @Deprecated("Since 201.4.0") var commitDialogCompletion: Boolean = true,
  @Deprecated("Since 201.4.0") var completionConfigs: List<CommitCompletionConfig> =
    arrayListOf(CommitCompletionConfig()),
  @Deprecated("Since 201.4.0") var referencePointForStatus: ReferencePointForStatusConfig =
    ReferencePointForStatusConfig(),
  @Deprecated("Since 201.4.0") var commitMessageValidation: Boolean = false,
  @Deprecated("Since 201.4.0") var commitMessageValidationRegex: String =
    "(?:fix|chore|docs|feat|refactor|style|test)(?:\\(.*\\))?: [A-Z].*\\s#\\d+",
  var autoFetchExclusionConfigs: List<AutoFetchExclusionConfig> = arrayListOf(),
  var autoFetchEnabledOverride: BoolValueOverride = BoolValueOverride(),
  var autoFetchIntervalMinutesOverride: IntValueOverride =
    IntValueOverride(false, AutoFetchParams.DEFAULT_INTERVAL_MINUTES),
  var autoFetchOnBranchSwitchOverride: BoolValueOverride = BoolValueOverride(),
  var commitDialogBranchCompletionOverride: BoolValueOverride = BoolValueOverride(),
  var commitDialogGitmojiCompletionOverride: BoolValueOverride = BoolValueOverride(),
  var commitDialogGitmojiUnicodeCompletionOverride: BoolValueOverride = BoolValueOverride(),
  var completionConfigsOverride: CommitCompletionConfigListOverride = CommitCompletionConfigListOverride(),
  var referencePointForStatusOverride: ReferencePointForStatusOverride = ReferencePointForStatusOverride(),
  var commitMessageValidationOverride: BoolValueOverride = BoolValueOverride(),
  var commitMessageValidationRegexOverride: StringValueOverride = StringValueOverride(
    value = "(?:fix|chore|docs|feat|refactor|style|test)(?:\\(.*\\))?: [A-Z].*\\s#\\d+"
  ),
  var outdatedBranchesCleanupOverride: OutdatedBranchesAutoCleanupOverride = OutdatedBranchesAutoCleanupOverride(),
  var outdatedBranchesExclusionGlobsOverride: ListValueOverride<String> = ListValueOverride(
    values = ArrayList(OutdatedBranchCleanupParams.EXCLUSIONS)
  )
) {

  @Transient
  fun copy(): GitToolBoxConfigPrj {
    return GitToolBoxConfigPrj(
      autoFetch,
      autoFetchIntervalMinutes,
      ArrayList(autoFetchExclusions),
      autoFetchOnBranchSwitch,
      commitDialogCompletion,
      completionConfigs.map { it.copy() },
      referencePointForStatus.copy(),
      commitMessageValidation,
      commitMessageValidationRegex,
      autoFetchExclusionConfigs.map { it.copy() },
      autoFetchEnabledOverride.copy(),
      autoFetchIntervalMinutesOverride.copy(),
      autoFetchOnBranchSwitchOverride.copy(),
      commitDialogBranchCompletionOverride.copy(),
      commitDialogGitmojiCompletionOverride.copy(),
      commitDialogGitmojiUnicodeCompletionOverride.copy(),
      completionConfigsOverride.copy(),
      referencePointForStatusOverride.copy(),
      commitMessageValidationOverride.copy(),
      commitMessageValidationRegexOverride.copy(),
      outdatedBranchesCleanupOverride.copy(),
      outdatedBranchesExclusionGlobsOverride.copy()
    )
  }
}
