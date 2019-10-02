package zielu.gittoolbox.ui.config.prj

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.SimpleTextAttributes.ERROR_ATTRIBUTES
import com.intellij.ui.SimpleTextAttributes.GRAYED_ATTRIBUTES
import git4idea.repo.GitRepository
import jodd.util.StringBand
import zielu.gittoolbox.config.AutoFetchExclusionConfig
import zielu.gittoolbox.config.RemoteConfig
import zielu.gittoolbox.util.GtUtil
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode

internal class AutoFetchExclusionTreeRenderer(private val project: Project): ColoredTreeCellRenderer() {
    override fun customizeCellRenderer(tree: JTree, value: Any?, selected: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean) {
        val userValue = (value as DefaultMutableTreeNode).userObject

        if (userValue is AutoFetchExclusionConfig) {
            val repository = GtUtil.getRepositoryForRoot(project, userValue.repositoryRootPath)
            if (repository.isPresent) {
                render(repository.get())
            } else {
                renderMissing(userValue.repositoryRootPath)
            }
        }
        if (userValue is RemoteConfig) {
            append(userValue.name)
        }
    }

    private fun render(repository: GitRepository) {
        append(GtUtil.name(repository))
        val url = StringBand(" (")
        url.append(repository.root.presentableUrl)
        url.append(")")
        append(url.toString(), GRAYED_ATTRIBUTES)
    }

    private fun renderMissing(value: String) {
        val path = VfsUtilCore.urlToPath(value)
        append(path, ERROR_ATTRIBUTES)
    }
}
