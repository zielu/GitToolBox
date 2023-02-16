package zielu.gittoolbox.ui.config.common

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.SimpleTextAttributes.ERROR_ATTRIBUTES
import com.intellij.ui.SimpleTextAttributes.EXCLUDED_ATTRIBUTES
import com.intellij.ui.SimpleTextAttributes.GRAYED_ATTRIBUTES
import git4idea.repo.GitRepository
import zielu.gittoolbox.ResBundle
import zielu.gittoolbox.cache.VirtualFileRepoCache
import zielu.gittoolbox.config.AutoFetchExclusionConfig
import zielu.gittoolbox.config.RemoteConfig
import zielu.gittoolbox.repo.createGtRepository
import zielu.gittoolbox.util.GtUtil
import java.util.Optional
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode

internal class AutoFetchExclusionTreeRenderer(private val project: Project) : ColoredTreeCellRenderer() {
  override fun customizeCellRenderer(
    tree: JTree,
    value: Any?,
    selected: Boolean,
    expanded: Boolean,
    leaf: Boolean,
    row: Int,
    hasFocus: Boolean
  ) {
    when (val userValue = (value as DefaultMutableTreeNode).userObject) {
      is AutoFetchExclusionConfig -> {
        val repository = findRepository(value)
        if (repository.isPresent) {
          render(repository.get(), userValue.hasRemotes())
        } else {
          renderMissing(userValue.repositoryRootPath)
        }
      }
      is RemoteConfig -> {
        val parentConfig = value.parent as DefaultMutableTreeNode
        val repository = findRepository(parentConfig)
        render(userValue, repository)
      }
    }
  }

  private fun findRepository(node: DefaultMutableTreeNode): Optional<GitRepository> {
    val userObject = node.userObject
    if (userObject is AutoFetchExclusionConfig) {
      return VirtualFileRepoCache.getInstance(project).findRepoForRoot(userObject.repositoryRootPath)
    }
    return Optional.empty()
  }

  private fun render(repository: GitRepository, remoteExclusions: Boolean) {
    append(GtUtil.name(repository))
    if (!remoteExclusions) {
      append(
        " ${ResBundle.message("configurable.prj.autoFetch.exclusions.all.remotes")}",
        EXCLUDED_ATTRIBUTES
      )
    }
    append(" (${repository.root.presentableUrl})", GRAYED_ATTRIBUTES)
  }

  private fun render(remote: RemoteConfig, repository: Optional<GitRepository>) {
    append(remote.name)
    if (repository.isPresent) {
      createGtRepository(repository.get()).findRemote(remote.name)?.firstUrl?.let { url ->
        append(" ($url)", GRAYED_ATTRIBUTES)
      }
    }
    toolTipText = null
  }

  private fun renderMissing(value: String) {
    val path = VfsUtilCore.urlToPath(value)
    append(path, ERROR_ATTRIBUTES)
    toolTipText = ResBundle.message("configurable.prj.autoFetch.exclusions.repo.not.found.tooltip")
  }
}
