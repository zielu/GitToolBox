package zielu.gittoolbox.ui.config.common

import zielu.gittoolbox.config.AutoFetchExclusionConfig
import zielu.gittoolbox.config.RemoteConfig
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath

internal class AutoFetchExclusionsTreeModel : DefaultTreeModel(DefaultMutableTreeNode()) {
  private val rootNode = DefaultMutableTreeNode()
  private var configs: MutableList<AutoFetchExclusionConfig> = ArrayList()

  init {
    setRoot(rootNode)
  }

  fun setConfigs(configs: List<AutoFetchExclusionConfig>) {
    rootNode.removeAllChildren()

    this.configs.clear()
    this.configs.addAll(configs)

    this.configs
      .map { configNode(it) }
      .sortedWith(ConfigsComparator)
      .forEach { rootNode.add(it) }

    nodeStructureChanged(rootNode)
  }

  fun getConfigs(): List<AutoFetchExclusionConfig> {
    return configs.toList()
  }

  private fun configNode(config: AutoFetchExclusionConfig): DefaultMutableTreeNode {
    val node = DefaultMutableTreeNode(config, true)
    config.excludedRemotes.stream()
      .map { remoteNode(it) }
      .forEach { node.add(it) }
    return node
  }

  private fun remoteNode(config: RemoteConfig): DefaultMutableTreeNode {
    return DefaultMutableTreeNode(config, false)
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

  fun getPathFor(config: AutoFetchExclusionConfig): TreePath? {
    val node = getNodeFor(config)
    return node?.path?.let { TreePath(it) }
  }

  private fun getNodeFor(config: AutoFetchExclusionConfig): DefaultMutableTreeNode? {
    return rootNode.children().toList()
      .map { it as DefaultMutableTreeNode }
      .find { it.userObject == config }
  }

  fun hasRemoteAt(path: TreePath): Boolean {
    val selected = path.lastPathComponent
    if (selected is DefaultMutableTreeNode) {
      return selected.userObject is RemoteConfig
    }
    return false
  }

  fun addRemotes(config: AutoFetchExclusionConfig, remotes: List<RemoteConfig>) {
    getNodeFor(config)?.let { configNode ->
      remotes.forEach {
        configNode.add(remoteNode(it))
      }
      if (remotes.isNotEmpty()) {
        nodeStructureChanged(configNode)
      }
    }
  }

  fun addConfigs(newConfigs: List<AutoFetchExclusionConfig>) {
    val existingConfigNodes = rootNode.children().toList().map { it as DefaultMutableTreeNode }
    val newNodes = newConfigs.map {
      configNode(it)
    }
    val mergedNodes = (existingConfigNodes + newNodes).sortedWith(ConfigsComparator)
    val newIndices = newNodes.map { mergedNodes.indexOf(it) }
    for ((index, node) in newNodes.withIndex()) {
      val insertAt = newIndices[index]
      rootNode.insert(node, insertAt)
      configs.add(insertAt, newConfigs[index])
    }
    nodeStructureChanged(rootNode)
  }
}

private object ConfigsComparator : Comparator<DefaultMutableTreeNode> {
  override fun compare(o1: DefaultMutableTreeNode, o2: DefaultMutableTreeNode): Int {
    val config1 = o1.userObject as AutoFetchExclusionConfig
    val config2 = o2.userObject as AutoFetchExclusionConfig

    return config1.repositoryRootPath.compareTo(config2.repositoryRootPath)
  }
}
