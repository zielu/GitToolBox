package zielu.gittoolbox.ui.config.common

import zielu.gittoolbox.config.AutoFetchExclusionConfig
import zielu.gittoolbox.config.RemoteConfig
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.MutableTreeNode
import javax.swing.tree.TreePath

internal class AutoFetchExclusionsTreeModel : DefaultTreeModel(DefaultMutableTreeNode()) {
  private var configs: MutableList<AutoFetchExclusionConfig> = ArrayList()

  fun setConfigs(configs: List<AutoFetchExclusionConfig>) {
    this.configs.clear()
    this.configs.addAll(configs)

    val rootNode = DefaultMutableTreeNode()
    this.configs.stream()
      .map { configNode(it) }
      .forEach { rootNode.add(it) }

    setRoot(rootNode)
  }

  private fun configNode(config: AutoFetchExclusionConfig): MutableTreeNode {
    val node = DefaultMutableTreeNode(config, true)
    config.excludedRemotes.stream()
      .map { DefaultMutableTreeNode(it, false) }
      .forEach { node.add(it) }
    return node
  }

  fun getConfigs(): List<AutoFetchExclusionConfig> {
    return ArrayList(configs)
  }

  fun getValue(value: Any?): Any? {
    if (value is DefaultMutableTreeNode) {
      return value.userObject
    }
    return null
  }

  fun removeAtPath(path: TreePath?) {
    path?.let {
      val selected = it.lastPathComponent
      if (selected is DefaultMutableTreeNode) {
        when (val selectedObject = selected.userObject) {
          is RemoteConfig -> {
            val parent = selected.parent as DefaultMutableTreeNode
            val config = parent.userObject as AutoFetchExclusionConfig
            if (config.removeRemote(selectedObject)) {
              parent.remove(selected)
              nodeStructureChanged(parent)
            }
          }
          is AutoFetchExclusionConfig -> {
            if (configs.remove(selectedObject)) {
              val parent = selected.parent as DefaultMutableTreeNode
              parent.remove(selected)
              nodeStructureChanged(parent)
            }
          }
        }
      }
    }
  }

  fun hasConfigAt(path: TreePath): Boolean {
    val selected = path.lastPathComponent
    if (selected is DefaultMutableTreeNode) {
      return selected.userObject is AutoFetchExclusionConfig
    }
    return false
  }

  fun getConfigAt(path: TreePath): AutoFetchExclusionConfig? {
    val selected = path.lastPathComponent
    if (selected is DefaultMutableTreeNode) {
      if (selected.userObject is AutoFetchExclusionConfig) {
        return selected.userObject as AutoFetchExclusionConfig
      }
    }
    return null
  }

  fun hasRemoteAt(path: TreePath): Boolean {
    val selected = path.lastPathComponent
    if (selected is DefaultMutableTreeNode) {
      return selected.userObject is RemoteConfig
    }
    return false
  }
}
