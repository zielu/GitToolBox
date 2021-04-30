package zielu.gittoolbox.config.override

import com.intellij.util.xmlb.annotations.Transient
import zielu.gittoolbox.config.ConfigItem
import zielu.gittoolbox.config.OutdatedBranchesCleanupConfig

internal data class OutdatedBranchesCleanupOverride(
  var enabled: Boolean = false,
  var value: OutdatedBranchesCleanupConfig = OutdatedBranchesCleanupConfig()
) : ConfigItem<OutdatedBranchesCleanupOverride> {

  @Transient
  override fun copy(): OutdatedBranchesCleanupOverride {
    return OutdatedBranchesCleanupOverride(
      enabled,
      value.copy()
    )
  }
}
