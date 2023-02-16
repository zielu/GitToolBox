package zielu.gittoolbox.config.override

import com.intellij.util.xmlb.annotations.Transient
import zielu.gittoolbox.config.ConfigItem
import zielu.gittoolbox.config.ReferencePointForStatusConfig

internal data class ReferencePointForStatusOverride(
  var enabled: Boolean = false,
  var value: ReferencePointForStatusConfig = ReferencePointForStatusConfig()
) : ConfigItem<ReferencePointForStatusOverride> {

  @Transient
  override fun copy(): ReferencePointForStatusOverride {
    return ReferencePointForStatusOverride(
      enabled,
      value.copy()
    )
  }
}
