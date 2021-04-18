package zielu.intellij.ui

import com.intellij.openapi.util.Disposer
import javax.swing.JComponent

internal class CompositeGtFormUiEx<T> : GtFormUiEx<T> {
  private val forms = arrayListOf<GtFormUiEx<T>>()

  fun add(form: GtFormUiEx<T>) {
    forms.add(form)
    Disposer.register(this, form)
  }

  override fun fillFromState(state: T) {
    forms.forEach { it.fillFromState(state) }
  }

  override fun isModified(): Boolean {
    return forms.any { it.isModified() }
  }

  override val content: JComponent
    get() = TODO("not implemented - should never be called")

  override fun afterStateSet() {
    forms.forEach { it.afterStateSet() }
  }

  override fun init() {
    forms.forEach { it.init() }
  }

  override fun dispose() {
    forms.clear()
  }

  override fun applyToState(state: T) {
    forms.forEach { it.applyToState(state) }
  }
}
