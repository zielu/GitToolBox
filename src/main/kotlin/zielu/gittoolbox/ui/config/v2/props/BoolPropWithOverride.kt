package zielu.gittoolbox.ui.config.v2.props

import com.intellij.openapi.Disposable
import com.intellij.openapi.observable.properties.BooleanProperty
import com.intellij.openapi.util.Disposer
import zielu.gittoolbox.config.override.BoolValueOverride
import zielu.intellij.ui.ZOnItemSelectable
import java.awt.ItemSelectable
import kotlin.reflect.KMutableProperty0

internal class BoolPropWithOverride(
  private val valueProperty: BooleanProperty,
  private val overrideProperty: BooleanProperty,
  private val appValue: KMutableProperty0<Boolean>,
  private val prjValue: BoolValueOverride,
  private val valueUi: (Boolean) -> Unit,
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

  override fun dispose() {
    Disposer.dispose(binding)
  }
}
