package zielu.gittoolbox.ui.config.v2.props

import com.intellij.openapi.Disposable
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.observable.properties.AtomicLazyProperty
import zielu.gittoolbox.config.override.IntValueOverride
import zielu.intellij.ui.ZOnItemSelectable
import java.awt.ItemSelectable
import kotlin.reflect.KMutableProperty0

internal class IntPropWithOverride(
  private val valueProperty: AtomicLazyProperty<Int>,
  private val overrideProperty: AtomicBooleanProperty,
  private val appValue: KMutableProperty0<Int>,
  private val prjValue: IntValueOverride,
  private val valueUi: (Int) -> Unit,
  overrideUi: ItemSelectable,
) : UiItem {
  private val binding: Disposable

  init {
    overrideProperty.set(prjValue.enabled)
    when (prjValue.enabled) {
      true -> valueProperty.set(prjValue.value)
      false -> valueProperty.set(appValue.get())
    }
    binding = ZOnItemSelectable(overrideUi) { onOverrideChange(it) }
  }

  private fun onOverrideChange(overridden: Boolean) {
    if (overridden) {
      valueUi.invoke(prjValue.value)
    } else {
      valueUi.invoke(appValue.invoke())
    }
  }

  override fun apply() {
    prjValue.enabled = overrideProperty.get()
    if (prjValue.enabled) {
      prjValue.value = valueProperty.get()
    }
  }
}
