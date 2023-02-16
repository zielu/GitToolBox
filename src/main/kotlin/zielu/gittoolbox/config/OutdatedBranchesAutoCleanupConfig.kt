package zielu.gittoolbox.config

import com.intellij.util.xmlb.annotations.Transient
import zielu.gittoolbox.branch.OutdatedBranchCleanupParams

internal data class OutdatedBranchesAutoCleanupConfig(
  var autoCheckEnabled: Boolean = false,
  var autoCheckIntervalHours: Int = OutdatedBranchCleanupParams.DEFAULT_INTERVAL_HOURS,
) : ConfigItem<OutdatedBranchesAutoCleanupConfig> {

  @Transient
  override fun copy(): OutdatedBranchesAutoCleanupConfig {
    return OutdatedBranchesAutoCleanupConfig(
      autoCheckEnabled,
      autoCheckIntervalHours
    )
  }
}
