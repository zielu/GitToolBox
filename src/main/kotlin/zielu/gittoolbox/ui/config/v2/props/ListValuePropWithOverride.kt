package zielu.gittoolbox.ui.config.v2.props

import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.ui.CollectionListModel
import zielu.gittoolbox.config.ConfigItem
import zielu.gittoolbox.config.override.ListValueOverride
import kotlin.reflect.KMutableProperty0

internal class ListValuePropWithOverride<T : ConfigItem<T>>(
  private val valueModel: CollectionListModel<T>,
  private val overrideProperty: AtomicBooleanProperty,
  private val appValue: KMutableProperty0<List<T>>,
  private val prjValue: ListValueOverride<T>
) : UiItem, ModifyTracker {
  init {
    overrideProperty.afterChange({ onOverrideChange(it) }, this)
    overrideProperty.set(prjValue.enabled)
  }

  private fun onOverrideChange(overridden: Boolean) {
    if (overridden) {
      valueModel.replaceAll(prjValue.values)
    } else {
      valueModel.replaceAll(appValue.invoke())
    }
  }

  override fun apply() {
    prjValue.enabled = overrideProperty.get()
    if (prjValue.enabled) {
      prjValue.values = valueModel.toList()
    }
  }

  override fun isModified(): Boolean {
    return if (overrideProperty.get()) {
      prjValue.values != valueModel.toList()
    } else {
      false
    }
  }
}
