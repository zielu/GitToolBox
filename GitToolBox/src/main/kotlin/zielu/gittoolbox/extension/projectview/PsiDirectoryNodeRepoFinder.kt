package zielu.gittoolbox.extension.projectview

import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode
import git4idea.repo.GitRepository

internal interface PsiDirectoryNodeRepoFinder {
  fun isForMe(directoryNode: PsiDirectoryNode): Boolean

  fun getRepoFor(directoryNode: PsiDirectoryNode): GitRepository?
}
