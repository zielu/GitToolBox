package zielu.gittoolbox.ui.config.v2

import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import kotlin.reflect.KMutableProperty0

internal class BoolProp(
  private val valueProperty: AtomicBooleanProperty,
  private val value: KMutableProperty0<Boolean>
) : UiItem {
  init {
    valueProperty.set(value.invoke())
  }

  override fun apply() {
    value.set(valueProperty.get())
  }
}
