package zielu.gittoolbox.ui.config.v2.shared

import com.intellij.ui.components.JBCheckBox
import zielu.gittoolbox.ResBundle

internal class OverrideCheckBoxes {
  private val checkBoxes = mutableListOf<JBCheckBox>()

  fun register(checkBox: JBCheckBox) {
    checkBoxes.add(checkBox)
    checkBox.toolTipText = ResBundle.message("common.override.tooltip")
  }

  fun hide() {
    checkBoxes.forEach { it.isVisible = false }
  }

  fun show() {
    checkBoxes.forEach { it.isVisible = true }
  }
}
