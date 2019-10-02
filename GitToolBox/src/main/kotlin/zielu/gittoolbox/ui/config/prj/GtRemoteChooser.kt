package zielu.gittoolbox.ui.config.prj

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.CollectionListModel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import zielu.gittoolbox.ResBundle
import java.awt.Component
import javax.swing.JComponent
import javax.swing.JPanel

internal class GtRemoteChooser(val project: Project, parentComponent: Component):
        DialogWrapper(project, parentComponent, false, IdeModalityType.PROJECT) {
    private val centerPanel: JPanel
    private val remoteList: JBList<String> = JBList()
    var remotes: MutableList<String> = ArrayList()
    var selectedRemotes: MutableList<String> = ArrayList()

    init {
        val scrollPane = JBScrollPane(remoteList)
        centerPanel = JBUI.Panels.simplePanel().addToCenter(scrollPane)
        centerPanel.preferredSize = JBUI.size(400, 300)
        title = ResBundle.message("configurable.prj.autoFetch.exclusions.remotes.add.title")
        init()
    }

    override fun createCenterPanel(): JComponent {
        return centerPanel
    }

    private fun fillData() {
        val remotesToShow = ArrayList(remotes)
        remotesToShow.removeAll(selectedRemotes)
        remotesToShow.sort()
        remoteList.model = CollectionListModel<String>(remotesToShow)
    }

    override fun show() {
        fillData()
        super.show()
    }

    override fun doOKAction() {
        selectedRemotes = remoteList.selectedValuesList
        super.doOKAction()
    }
}
