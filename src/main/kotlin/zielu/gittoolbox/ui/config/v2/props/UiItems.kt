package zielu.gittoolbox.ui.config.v2.props

import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer

internal class UiItems : Disposable {
  private val items = mutableListOf<UiItem>()

  fun register(vararg item: UiItem) {
    items.addAll(item)
  }

  fun apply() {
    items.forEach(UiItem::apply)
  }

  fun clear() {
    items.forEach { Disposer.dispose(it) }
    items.clear()
  }

  override fun dispose() {
    clear()
  }
}
