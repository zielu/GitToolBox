package zielu.gittoolbox.ui.actions

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import git4idea.actions.GitRepositoryAction
import zielu.gittoolbox.ResBundle
import zielu.gittoolbox.cache.VirtualFileRepoCache
import zielu.gittoolbox.repo.createGtRepository
import zielu.gittoolbox.ui.branch.BranchUiService

internal class SwitchRecentBranchesPopupAction : GitRepositoryAction() {
  override fun perform(project: Project, gitRoots: MutableList<VirtualFile>, defaultRoot: VirtualFile) {
    val virtualFileRepoCache = VirtualFileRepoCache.getInstance(project)
    if (gitRoots.size == 1) {
      val repo = virtualFileRepoCache.getRepoForRoot(gitRoots[0])
      repo?.let {
        BranchUiService.getInstance(project).showRecentBranchesSwitcher(createGtRepository(repo))
      }
    } else {
      val repositories = gitRoots.asSequence()
        .mapNotNull { virtualFileRepoCache.getRepoForRoot(it) }
        .map { createGtRepository(it) }
        .toList()
      if (repositories.isNotEmpty()) {
        BranchUiService.getInstance(project).showRecentBranchesSwitcher(repositories)
      }
    }
  }

  override fun getActionName(): String = ResBundle.message("switch.recentBranches.popup.action")
}
