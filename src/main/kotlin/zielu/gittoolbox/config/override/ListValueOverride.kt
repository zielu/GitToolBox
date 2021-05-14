package zielu.gittoolbox.config.override

import com.intellij.util.xmlb.annotations.Transient
import zielu.gittoolbox.config.ConfigItem

internal data class ListValueOverride<T>(
  var enabled: Boolean = false,
  var values: List<T> = arrayListOf()
) : ConfigItem<ListValueOverride<T>> {

  @Transient
  override fun copy(): ListValueOverride<T> {
    return ListValueOverride(
      enabled,
      ArrayList(values)
    )
  }
}
