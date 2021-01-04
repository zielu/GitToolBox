package zielu.gittoolbox.ui.config.v2

import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.observable.properties.AtomicLazyProperty
import zielu.gittoolbox.config.override.IntValueOverride
import kotlin.reflect.KMutableProperty0

internal class IntPropWithOverride(
  private val valueProperty: AtomicLazyProperty<Int>,
  private val overrideProperty: AtomicBooleanProperty,
  private val appValue: KMutableProperty0<Int>,
  private val prjValue: IntValueOverride
) : UiItem {
  init {
    overrideProperty.afterChange { onOverrideChange(it) }
    overrideProperty.set(prjValue.enabled)
  }

  private fun onOverrideChange(overridden: Boolean) {
    if (overridden) {
      valueProperty.set(prjValue.value)
    } else {
      valueProperty.set(appValue.invoke())
    }
  }

  override fun apply() {
    prjValue.enabled = overrideProperty.get()
    if (prjValue.enabled) {
      prjValue.value = valueProperty.get()
    } else {
      appValue.set(valueProperty.get())
    }
  }
}
