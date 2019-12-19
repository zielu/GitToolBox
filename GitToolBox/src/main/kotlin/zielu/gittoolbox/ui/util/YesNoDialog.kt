package zielu.gittoolbox.ui.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.MultiLineLabelUI
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class YesNoDialog(
  project: Project, parent: Component, title: String, val message: String
) : DialogWrapper(project, parent, false, IdeModalityType.PROJECT) {

  init {
    setTitle(title)
  }

  override fun createNorthPanel(): JComponent? {
    val panel = JPanel(BorderLayout(15, 0))
    val textLabel = JLabel(message)
    textLabel.setUI(MultiLineLabelUI())
    panel.add(textLabel, BorderLayout.CENTER)
    return panel;
  }

  override fun createCenterPanel(): JComponent? = null

  fun makeCancelDefault() {
    okAction.putValue(DEFAULT_ACTION, null)
    cancelAction.putValue(DEFAULT_ACTION, true)
  }

  override fun show() {
    init()
    super.show()
  }
}
