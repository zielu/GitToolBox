package zielu.gittoolbox.extension.projectview

import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode
import com.intellij.openapi.extensions.ExtensionPointName
import git4idea.repo.GitRepository
import zielu.intellij.java.firstOrNull
import java.util.stream.Stream

internal class ViewPsiDirectoryNodeExtension {
  fun findForDirectory(node: ProjectViewNode<*>): GitRepository? {
    val directoryNode = node as PsiDirectoryNode
    return finders().firstOrNull { it.isForMe(directoryNode) }?.getRepoFor(directoryNode)
  }

  private fun finders(): Stream<PsiDirectoryNodeRepoFinder> {
    return EXTENSION_POINT_NAME.extensionList.stream().map { it.instantiate() }
  }
}

private val EXTENSION_POINT_NAME: ExtensionPointName<ViewPsiDirectoryNodeEP> = ExtensionPointName.create(
  "zielu.gittoolbox.viewPsiDirectoryNode"
)
