package zielu.gittoolbox.config.override

import com.intellij.util.xmlb.annotations.Transient

internal data class EnumValueOverride<T : Enum<T>>(
  var enabled: Boolean = false,
  var value: T
) {

  @Transient
  fun copy(): EnumValueOverride<T> {
    return EnumValueOverride(
      enabled,
      value
    )
  }
}
