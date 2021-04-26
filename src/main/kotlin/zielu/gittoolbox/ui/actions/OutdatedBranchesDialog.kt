package zielu.gittoolbox.ui.actions

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.CheckboxTree
import com.intellij.ui.CheckedTreeNode
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.TreeExpandCollapse
import com.intellij.ui.components.JBScrollPane
import git4idea.repo.GitRepository
import jodd.util.StringBand
import zielu.gittoolbox.branch.OutdatedBranch
import zielu.gittoolbox.branch.OutdatedReason
import zielu.gittoolbox.config.DateType
import zielu.gittoolbox.ui.DatePresenter
import zielu.gittoolbox.util.GtUtil
import java.awt.BorderLayout
import java.time.ZonedDateTime
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.tree.DefaultTreeModel

internal class OutdatedBranchesDialog(
  project: Project
) : DialogWrapper(
  project,
  false,
  IdeModalityType.PROJECT
) {

  private lateinit var tree: CheckboxTree
  private var selected: Map<GitRepository, List<OutdatedBranch>> = mapOf()

  init {
    title = "Git Branches Cleanup"
    init()
    setSize(400, 300)
  }

  override fun createCenterPanel(): JComponent {
    // TODO: CheckboxTreeTable
    tree = CheckboxTree(createRenderer(), CheckedTreeNode(RootNode()))
    tree.isRootVisible = false
    val panel = JPanel(BorderLayout())
    panel.add(JBScrollPane(tree), BorderLayout.CENTER)
    return panel
  }

  private fun createRenderer(): CheckboxTree.CheckboxTreeCellRenderer {
    return object : CheckboxTree.CheckboxTreeCellRenderer() {
      override fun customizeRenderer(
        tree: JTree,
        value: Any,
        selected: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean
      ) {
        (value as CheckedTreeNode).userObject?.apply {
          val node = this as MyNode
          val text = textRenderer
          text.append(node.getText())
          node.getSubText()?.apply {
            text.append(" $this", SimpleTextAttributes.GRAYED_ATTRIBUTES)
          }
        }
      }
    }
  }

  fun setData(data: Map<GitRepository, List<OutdatedBranch>>) {
    val root = buildNodes(data)
    tree.model = DefaultTreeModel(root)
    TreeExpandCollapse.expandAll(tree)
  }

  private fun buildNodes(data: Map<GitRepository, List<OutdatedBranch>>): CheckedTreeNode {
    val root = CheckedTreeNode(RootNode())
    data.forEach {
      val repo = it.key
      val repoNode = CheckedTreeNode(RepoNode(repo))
      it.value.forEach { branch ->
        repoNode.add(CheckedTreeNode(BranchNode(repo, branch)))
      }
      root.add(repoNode)
    }
    return root
  }

  override fun doOKAction() {
    captureSelection()
    super.doOKAction()
  }

  private fun captureSelection() {
    val selectedBranches = tree.getCheckedNodes(BranchNode::class.java) {
      true
    }
    val selection = mutableMapOf<GitRepository, MutableList<OutdatedBranch>>()
    selectedBranches.forEach {
      val branches = selection.getOrDefault(it.repo, mutableListOf())
      branches.add(it.branch)
      selection[it.repo] = branches
    }

    selected = selection.toMap()
  }

  fun getData(): Map<GitRepository, List<OutdatedBranch>> = selected
}

private interface MyNode {
  fun getText(): String
  fun getSubText(): String? = null
}

private class RootNode : MyNode {
  override fun getText(): String = ""
}

private data class RepoNode(
  private val repo: GitRepository
) : MyNode {
  override fun getText(): String = GtUtil.name(repo)
}

private data class BranchNode(
  val repo: GitRepository,
  val branch: OutdatedBranch
) : MyNode {
  override fun getText(): String = branch.getName()

  override fun getSubText(): String {
    val text = StringBand()
    branch.latestCommitTimestamp?.apply { text.append(" ${formatDate(this)}") }
    branch.getRemoteBranchName()?.apply { text.append(" (${this})") }
    return text.toString()
  }

  private fun formatDate(dateTime: ZonedDateTime): String {
    return DatePresenter.getInstance().format(DateType.RELATIVE, dateTime)
  }
}
