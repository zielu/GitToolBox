package zielu.gittoolbox.config.override

internal data class AppliedConfigOverride(
  var projectPath: String
) {

  fun copy(): AppliedConfigOverride {
    return AppliedConfigOverride(
      projectPath
    )
  }
}
