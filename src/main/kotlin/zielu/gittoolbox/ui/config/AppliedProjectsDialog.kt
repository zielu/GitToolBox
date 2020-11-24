package zielu.gittoolbox.ui.config

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.CollectionListModel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import zielu.gittoolbox.ResBundle
import java.awt.Component
import javax.swing.Action
import javax.swing.JComponent

internal class AppliedProjectsDialog(parent: Component) : DialogWrapper(parent, false) {
  private val pathList = JBList<String>()
  private val centerPanel: JComponent

  private val paths: MutableList<String> = arrayListOf()

  init {
    val scrollPane = JBScrollPane(pathList)
    centerPanel = JBUI.Panels.simplePanel().addToCenter(scrollPane)
    centerPanel.setPreferredSize(JBUI.size(400, 300))
    title = ResBundle.message("configurable.extras.applied.title")

    init()
  }

  override fun createCenterPanel(): JComponent = centerPanel

  override fun createActions(): Array<Action> {
    return arrayOf(okAction)
  }

  override fun show() {
    fillData()
    super.show()
  }

  private fun fillData() {
    pathList.model = CollectionListModel(paths)
  }

  fun setAppliedPaths(paths: List<String>) {
    this.paths.clear()
    this.paths.addAll(paths)
  }
}
