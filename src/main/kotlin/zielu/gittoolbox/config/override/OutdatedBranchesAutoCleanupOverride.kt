package zielu.gittoolbox.config.override

import com.intellij.util.xmlb.annotations.Transient
import zielu.gittoolbox.config.ConfigItem
import zielu.gittoolbox.config.OutdatedBranchesAutoCleanupConfig

internal data class OutdatedBranchesAutoCleanupOverride(
  var enabled: Boolean = false,
  var value: OutdatedBranchesAutoCleanupConfig = OutdatedBranchesAutoCleanupConfig()
) : ConfigItem<OutdatedBranchesAutoCleanupOverride> {

  @Transient
  override fun copy(): OutdatedBranchesAutoCleanupOverride {
    return OutdatedBranchesAutoCleanupOverride(
      enabled,
      value.copy()
    )
  }
}
