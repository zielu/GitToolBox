package zielu.gittoolbox.config.override

import com.intellij.util.xmlb.annotations.Transient
import zielu.gittoolbox.config.ConfigItem

internal data class IntValueOverride(
  var enabled: Boolean = false,
  var value: Int = 0
) : ConfigItem<IntValueOverride> {

  @Transient
  override fun copy(): IntValueOverride {
    return IntValueOverride(
      enabled,
      value
    )
  }
}
