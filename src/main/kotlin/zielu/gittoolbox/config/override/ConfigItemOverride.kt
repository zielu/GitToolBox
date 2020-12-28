package zielu.gittoolbox.config.override

import com.intellij.util.xmlb.annotations.Transient
import zielu.gittoolbox.config.ConfigItem

internal data class ConfigItemOverride<T : ConfigItem<T>>(
  var enabled: Boolean = false,
  var value: T? = null
) {

  @Transient
  fun copy(): ConfigItemOverride<T> {
    return ConfigItemOverride(
      enabled,
      value
    )
  }
}
