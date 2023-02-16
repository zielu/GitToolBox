package zielu.gittoolbox.config.override

import com.intellij.util.xmlb.annotations.Transient
import zielu.gittoolbox.config.ConfigItem

internal data class BoolValueOverride(
  var enabled: Boolean = false,
  var value: Boolean = false
) : ConfigItem<BoolValueOverride> {

  @Transient
  override fun copy(): BoolValueOverride {
    return BoolValueOverride(
      enabled,
      value
    )
  }
}
