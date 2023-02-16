package zielu.gittoolbox.ui.config.v2.props

import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer

internal class UiItems : Disposable, ModifyTracker {
  private val items = mutableListOf<UiItem>()
  private val modifyTrackers = mutableListOf<ModifyTracker>()

  fun register(vararg item: UiItem) {
    items.addAll(item)
    item.forEach {
      if (it is ModifyTracker) {
        modifyTrackers.add(it)
      }
    }
  }

  fun apply() {
    items.forEach(UiItem::apply)
  }

  fun clear() {
    items.forEach { Disposer.dispose(it) }
    items.clear()
    modifyTrackers.clear()
  }

  override fun dispose() {
    clear()
  }

  override fun isModified(): Boolean {
    return modifyTrackers.any { it.isModified() }
  }
}
