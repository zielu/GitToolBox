package zielu.gittoolbox.ui.config.v2.props

import com.intellij.openapi.observable.properties.AtomicLazyProperty
import kotlin.reflect.KMutableProperty0

internal class ValueProp<T>(
  private val valueProperty: AtomicLazyProperty<T>,
  getVal: () -> T,
  private val setVal: (T) -> Unit
) : UiItem {
  init {
    valueProperty.set(getVal.invoke())
  }

  constructor(
    valueProperty: AtomicLazyProperty<T>,
    value: KMutableProperty0<T>
  ) : this(
    valueProperty,
    value::get,
    value::set
  )

  override fun apply() {
    setVal.invoke(valueProperty.get())
  }
}
