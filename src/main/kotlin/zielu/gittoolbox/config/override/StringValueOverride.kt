package zielu.gittoolbox.config.override

import com.intellij.util.xmlb.annotations.Transient
import zielu.gittoolbox.config.ConfigItem

internal data class StringValueOverride(
  var enabled: Boolean = false,
  var value: String = ""
) : ConfigItem<StringValueOverride> {

  @Transient
  override fun copy(): StringValueOverride {
    return StringValueOverride(
      enabled,
      value
    )
  }
}
