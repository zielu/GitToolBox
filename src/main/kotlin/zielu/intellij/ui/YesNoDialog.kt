package zielu.intellij.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.MultiLineLabelUI
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Container
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class YesNoDialog(
  project: Project,
  parent: Component,
  title: String,
  private val message: String,
  private val icon: Icon? = null
) : DialogWrapper(project, parent, false, IdeModalityType.PROJECT) {

  constructor(
    project: Project,
    parent: Component,
    title: String,
    message: String
  ) : this(project, parent, title, message, null)

  init {
    setTitle(title)
  }

  override fun createNorthPanel(): JComponent? {
    val panel = JPanel(BorderLayout(15, 0))
    if (icon != null) {
      val iconLabel = JLabel(icon)
      val container = Container()
      container.layout = BorderLayout()
      container.add(iconLabel, BorderLayout.NORTH)
      panel.add(container, BorderLayout.WEST)
    }

    val textLabel = JLabel(message)
    textLabel.setUI(MultiLineLabelUI())
    panel.add(textLabel, BorderLayout.CENTER)
    return panel
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
