package zielu.gittoolbox.ui.config.v2.props

import com.intellij.ui.CollectionListModel
import zielu.gittoolbox.config.ConfigItem
import kotlin.reflect.KMutableProperty0

internal class ListValueProp<T : ConfigItem<T>>(
  private val valueModel: CollectionListModel<T>,
  private val value: KMutableProperty0<List<T>>
) : UiItem, ModifyTracker {
  init {
    valueModel.replaceAll(value.invoke())
  }

  override fun apply() {
    value.set(valueModel.toList())
  }

  override fun isModified(): Boolean {
    return value.invoke() != valueModel.toList()
  }
}
