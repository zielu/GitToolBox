package zielu.gittoolbox.config

internal data class ExtrasConfig

constructor(
  var autoFetchEnabledOverride: BoolConfigOverride = BoolConfigOverride(),
  var autoFetchOnBranchSwitchOverride: BoolConfigOverride = BoolConfigOverride()
) {

  fun copy(): ExtrasConfig {
    return ExtrasConfig(
      autoFetchEnabledOverride.copy(),
      autoFetchOnBranchSwitchOverride.copy()
    )
  }
}
