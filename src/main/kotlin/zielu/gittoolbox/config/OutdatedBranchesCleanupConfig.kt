package zielu.gittoolbox.config

internal data class OutdatedBranchesCleanupConfig(
  var autoCheckEnabled: Boolean = true,
  var autoCheckIntervalHours: Int = 4,
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
