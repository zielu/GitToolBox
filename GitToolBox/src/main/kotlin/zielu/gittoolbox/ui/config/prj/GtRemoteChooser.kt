package zielu.gittoolbox.ui.config.prj

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.CollectionListModel
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import git4idea.repo.GitRemote
import zielu.gittoolbox.ResBundle
import java.awt.Component
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.JPanel

internal class GtRemoteChooser(val project: Project, parentComponent: Component) :
  DialogWrapper(project, parentComponent, false, IdeModalityType.PROJECT) {
  private val centerPanel: JPanel
  private val remoteList: JBList<GitRemote> = JBList()
  var remotes: Collection<GitRemote> = ArrayList()
  var selectedRemotes: MutableList<String> = ArrayList()
  var repositoryName: String = ""

  init {
    remoteList.cellRenderer = RemoteRenderer()
    val scrollPane = JBScrollPane(remoteList)
    centerPanel = JBUI.Panels.simplePanel().addToCenter(scrollPane)
    centerPanel.preferredSize = JBUI.size(400, 300)
    title = ResBundle.message("configurable.prj.autoFetch.exclusions.remotes.add.title", repositoryName)
    init()
  }

  override fun createCenterPanel(): JComponent {
    return centerPanel
  }

  private fun fillData() {
    val remotesToShow = remotes.filter { it.name !in selectedRemotes }.sorted()
    remoteList.model = CollectionListModel<GitRemote>(remotesToShow)
  }

  override fun show() {
    fillData()
    super.show()
  }

  override fun doOKAction() {
    selectedRemotes = remoteList.selectedValuesList.map { it.name }.toMutableList()
    super.doOKAction()
  }
}

private class RemoteRenderer: ColoredListCellRenderer<GitRemote>() {
  override fun customizeCellRenderer(list: JList<out GitRemote>, value: GitRemote?, index: Int, selected: Boolean, hasFocus: Boolean) {
    value?.apply {
      append(name)
    }?.firstUrl?.let { url ->
      append(" ($url)", SimpleTextAttributes.GRAYED_ATTRIBUTES)
    }
  }
}
