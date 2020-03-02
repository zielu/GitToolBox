package zielu.intellij.ui

import zielu.intellij.util.ZProperty
import javax.swing.JCheckBox

internal object ZUiProperties {
  @JvmStatic
  fun createSelectedProperty(checkBox: JCheckBox): ZProperty<Boolean> = ZBoolCheckBoxSelectedProperty(checkBox)
}

private class ZBoolCheckBoxSelectedProperty(private val checkBox: JCheckBox) : ZProperty<Boolean> {
  override fun get(): Boolean = checkBox.isSelected

  override fun set(value: Boolean) {
    checkBox.isSelected = value
  }
}
