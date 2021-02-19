package zielu.gittoolbox.ui.config.v2.props

import com.intellij.openapi.Disposable
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.observable.properties.AtomicLazyProperty
import com.intellij.openapi.util.Disposer
import zielu.intellij.ui.ZOnItemSelectable
import java.awt.ItemSelectable
import kotlin.reflect.KMutableProperty0

internal class ValuePropWithOverride<T>(
  private val valueProperty: AtomicLazyProperty<T>,
  private val overrideProperty: AtomicBooleanProperty,
  private val appValue: KMutableProperty0<T>,
  private val prjOverridden: KMutableProperty0<Boolean>,
  private val prjValue: KMutableProperty0<T>,
  private val valueUi: (T) -> Unit,
  overrideUi: ItemSelectable
) : UiItem {
  private val binding: Disposable

  init {
    overrideProperty.set(prjOverridden.get())
    binding = ZOnItemSelectable(overrideUi) { onOverrideChange(it) }
  }

  private fun onOverrideChange(overridden: Boolean) {
    if (overridden) {
      valueUi.invoke(prjValue.get())
    } else {
      valueUi.invoke(appValue.invoke())
    }
  }

  override fun apply() {
    prjOverridden.set(overrideProperty.get())
    if (prjOverridden.get()) {
      prjValue.set(valueProperty.get())
    }
  }

  override fun dispose() {
    Disposer.dispose(binding)
  }
}
