package zielu.gittoolbox.ui.config.v2

import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.observable.properties.AtomicLazyProperty
import kotlin.reflect.KMutableProperty0

internal class RefPropWithOverride<T>(
  private val valueProperty: AtomicLazyProperty<T>,
  private val overrideProperty: AtomicBooleanProperty,
  private val appValue: KMutableProperty0<T>,
  private val prjOverridden: KMutableProperty0<Boolean>,
  private val prjValue: KMutableProperty0<T>
) : UiItem {
  init {
    overrideProperty.afterChange { onOverrideChange(it) }
    overrideProperty.set(prjOverridden.get())
  }

  private fun onOverrideChange(overridden: Boolean) {
    if (overridden) {
      valueProperty.set(prjValue.get())
    } else {
      valueProperty.set(appValue.invoke())
    }
  }

  override fun apply() {
    prjOverridden.set(overrideProperty.get())
    if (prjOverridden.get()) {
      prjValue.set(valueProperty.get())
    } else {
      appValue.set(valueProperty.get())
    }
  }
}
