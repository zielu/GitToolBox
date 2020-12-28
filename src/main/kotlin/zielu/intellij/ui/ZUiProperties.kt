package zielu.intellij.ui

import zielu.intellij.util.ZProperty
import javax.swing.JCheckBox

internal object ZUiProperties {
  @JvmStatic
  fun createSelectedProperty(checkBox: JCheckBox): ZProperty<Boolean> = ZBoolCheckBoxSelectedProperty(checkBox)
}

private class ZBoolCheckBoxSelectedProperty(private val checkBox: JCheckBox) : ZProperty<Boolean> {
  override var value: Boolean
    get() = checkBox.isSelected
    set(value) {
      checkBox.isSelected = value
    }
}
