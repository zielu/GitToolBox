package zielu.gittoolbox.config

import zielu.gittoolbox.branch.OutdatedBranchCleanupParams

internal data class OutdatedBranchesCleanupConfig(
  var autoCheckEnabled: Boolean = true,
  var autoCheckIntervalHours: Int = OutdatedBranchCleanupParams.DEFAULT_INTERVAL_HOURS,
  var exclusionGlobs: List<String> = arrayListOf("master", "main")
) : ConfigItem<OutdatedBranchesCleanupConfig> {
  override fun copy(): OutdatedBranchesCleanupConfig {
    return OutdatedBranchesCleanupConfig(
      autoCheckEnabled,
      autoCheckIntervalHours,
      exclusionGlobs.toMutableList()
    )
  }
}
