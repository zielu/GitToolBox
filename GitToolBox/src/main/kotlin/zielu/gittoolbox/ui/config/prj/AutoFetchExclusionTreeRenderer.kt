package zielu.gittoolbox.ui.config.prj

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.SimpleTextAttributes.ERROR_ATTRIBUTES
import com.intellij.ui.SimpleTextAttributes.GRAYED_ATTRIBUTES
import git4idea.repo.GitRepository
import jodd.util.StringBand
import zielu.gittoolbox.ResBundle
import zielu.gittoolbox.config.AutoFetchExclusionConfig
import zielu.gittoolbox.config.RemoteConfig
import zielu.gittoolbox.util.GtUtil
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
        val repository = GtUtil.getRepositoryForRoot(project, userValue.repositoryRootPath)
        if (repository.isPresent) {
          render(repository.get())
        } else {
          renderMissing(userValue.repositoryRootPath)
        }
      }
      is RemoteConfig -> render(userValue)
    }
  }

  private fun render(repository: GitRepository) {
    append(GtUtil.name(repository))
    val url = StringBand(" (")
    url.append(repository.root.presentableUrl)
    url.append(")")
    append(url.toString(), GRAYED_ATTRIBUTES)
  }

  private fun render(remote: RemoteConfig) {
    append(ResBundle.message("message.remote.label") + " ", GRAYED_ATTRIBUTES)
    append(remote.name)
  }

  private fun renderMissing(value: String) {
    val path = VfsUtilCore.urlToPath(value)
    append(path, ERROR_ATTRIBUTES)
  }
}
