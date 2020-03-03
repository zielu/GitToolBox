package zielu.gittoolbox.config

import com.intellij.util.xmlb.annotations.Transient

internal data class ExtrasConfig(
  var autoFetchEnabledOverride: BoolConfigOverride = BoolConfigOverride(),
  var autoFetchOnBranchSwitchOverride: BoolConfigOverride = BoolConfigOverride()
) {

  @Transient
  fun copy(): ExtrasConfig {
    return ExtrasConfig(
      autoFetchEnabledOverride.copy(),
      autoFetchOnBranchSwitchOverride.copy()
    )
  }
}
