package zielu.gittoolbox.config

import com.intellij.util.xmlb.annotations.Transient

internal data class AppliedConfigOverride(
  var projectPath: String = ""
) {

  @Transient
  fun copy(): AppliedConfigOverride {
    return AppliedConfigOverride(
      projectPath
    )
  }
}
