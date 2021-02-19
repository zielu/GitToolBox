package zielu.intellij.ui

import com.intellij.openapi.Disposable
import java.awt.ItemSelectable
import java.awt.event.ItemEvent
import java.awt.event.ItemListener

internal class ZOnItemSelectable(
  itemSelectable: ItemSelectable,
  private val action: (Boolean) -> Unit
) :ItemListener, Disposable {
  private val onDispose = { itemSelectable.removeItemListener(this) }

  init {
    itemSelectable.addItemListener(this)
  }

  override fun itemStateChanged(e: ItemEvent) {
    action.invoke(e.itemSelectable.selectedObjects != null)
  }

  override fun dispose() {
    onDispose.invoke()
  }
}
