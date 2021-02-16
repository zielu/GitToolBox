package zielu.gittoolbox.ui.config.v2.props

import com.intellij.openapi.observable.properties.AtomicLazyProperty
import kotlin.reflect.KMutableProperty0

internal class ValueProp<T>(
  private val valueProperty: AtomicLazyProperty<T>,
  private val value: KMutableProperty0<T>
) : UiItem {
  init {
    valueProperty.set(value.invoke())
  }

  override fun apply() {
    value.set(valueProperty.get())
  }
}
