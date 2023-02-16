package zielu.intellij.test

import java.awt.ItemSelectable
import java.awt.event.ItemEvent
import java.awt.event.ItemListener

internal class MockItemSelectable : ItemSelectable {
  private val listeners = mutableListOf<ItemListener>()
  private var selected: Array<Any>? = null

  fun setSelectedObjects(selected: Array<Any>?) {
    this.selected = selected
  }

  fun fireSelected() {
    val item = if (selected != null) selected!![0] else null
    val event = ItemEvent(
      this,
      ItemEvent.ITEM_STATE_CHANGED,
      item,
      ItemEvent.SELECTED
    )
    listeners.forEach { it.itemStateChanged(event) }
  }

  override fun getSelectedObjects(): Array<Any>? {
    return selected
  }

  override fun addItemListener(l: ItemListener) {
    listeners.add(l)
  }

  override fun removeItemListener(l: ItemListener) {
    listeners.remove(l)
  }
}
