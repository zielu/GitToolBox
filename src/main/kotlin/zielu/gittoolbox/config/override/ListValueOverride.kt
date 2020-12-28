package zielu.gittoolbox.config.override

import com.intellij.util.xmlb.annotations.Transient
import zielu.gittoolbox.config.ConfigItem

internal data class ListValueOverride<T : ConfigItem<T>>(
  var enabled: Boolean = false,
  var values: List<T> = arrayListOf()
) {

  @Transient
  fun copy(): ListValueOverride<T> {
    return ListValueOverride(
      enabled,
      values.map { it.copy() }
    )
  }
}
