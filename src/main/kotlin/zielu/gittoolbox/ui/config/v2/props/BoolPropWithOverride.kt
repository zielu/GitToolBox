package zielu.gittoolbox.ui.config.v2.props

import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import zielu.gittoolbox.config.override.BoolValueOverride
import kotlin.reflect.KMutableProperty0

internal class BoolPropWithOverride(
  private val valueProperty: AtomicBooleanProperty,
  private val overrideProperty: AtomicBooleanProperty,
  private val appValue: KMutableProperty0<Boolean>,
  private val prjValue: BoolValueOverride
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
