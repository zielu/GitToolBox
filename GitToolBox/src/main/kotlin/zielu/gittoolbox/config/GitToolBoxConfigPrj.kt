package zielu.gittoolbox.config

import com.intellij.util.xmlb.annotations.Transient
import zielu.gittoolbox.fetch.AutoFetchParams
import zielu.gittoolbox.formatter.Formatter

internal data class GitToolBoxConfigPrj(
  var autoFetch: Boolean = true,
  var autoFetchIntervalMinutes: Int = AutoFetchParams.DEFAULT_INTERVAL_MINUTES,
  @Deprecated("Since 192.3.1") var autoFetchExclusions: List<String> = ArrayList(),
  var autoFetchExclusionConfigs: List<AutoFetchExclusionConfig> = ArrayList(),
  var autoFetchOnBranchSwitch: Boolean = true,
  var commitDialogCompletion: Boolean = true,
  var completionConfigs: List<CommitCompletionConfig> = arrayListOf(CommitCompletionConfig()),
  var referencePointForStatus: ReferencePointForStatusConfig = ReferencePointForStatusConfig(),
  var commitMessageValidation: Boolean = false,
  var commitMessageValidationRegex: String = "(?:fix|chore|docs|feat|refactor|style|test)(?:\\(.*\\))?: [A-Z].*\\s#\\d+"
) {

  @Transient
  fun copy(): GitToolBoxConfigPrj {
    return GitToolBoxConfigPrj(
      autoFetch,
      autoFetchIntervalMinutes,
      autoFetchExclusions,
      autoFetchExclusionConfigs.map { it.copy() },
      autoFetchOnBranchSwitch,
      commitDialogCompletion,
      completionConfigs.map { it.copy() },
      referencePointForStatus.copy(),
      commitMessageValidation,
      commitMessageValidationRegex
    )
  }

  @Transient
  fun getCompletionFormatters(): List<Formatter> {
    return completionConfigs.map { it.createFormatter() }
  }

  fun isReferencePointForStatusChanged(other: GitToolBoxConfigPrj): Boolean {
    return referencePointForStatus != other.referencePointForStatus
  }
}
