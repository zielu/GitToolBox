package zielu.gittoolbox.ui.projectview.android

import com.android.tools.idea.navigator.nodes.android.AndroidPsiDirectoryNode
import com.android.tools.idea.navigator.nodes.android.AndroidSourceTypeNode
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode
import git4idea.repo.GitRepository
import zielu.gittoolbox.cache.VirtualFileRepoCache
import zielu.gittoolbox.extension.projectview.PsiDirectoryNodeRepoFinder

internal class AndroidViewPsiDirectoryNodeRepoFinder : PsiDirectoryNodeRepoFinder {
  override fun isForMe(directoryNode: PsiDirectoryNode): Boolean {
    return directoryNode is AndroidPsiDirectoryNode && directoryNode.parent is AndroidSourceTypeNode
  }

  override fun getRepoFor(directoryNode: PsiDirectoryNode): GitRepository? {
    val cache = VirtualFileRepoCache.getInstance(directoryNode.project!!)
    return directoryNode.virtualFile?.let { cache.getRepoForDir(it) }
  }
}
