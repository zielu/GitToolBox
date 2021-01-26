package zielu.gittoolbox.ui.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.IssueNavigationConfiguration
import com.intellij.openapi.vfs.VirtualFile
import zielu.gittoolbox.cache.PerRepoInfoCache
import zielu.gittoolbox.cache.VirtualFileRepoCache

internal class OpenBranchIssueActionGroup : GitToolboxActionGroup() {
  override fun getChildren(e: AnActionEvent?): Array<out AnAction> {
    val children = super.getChildren(e).toMutableList()
    children.addAll(e?.let { getActions(it) } ?: emptyList())
    return children.toTypedArray()
  }

  private fun getActions(e: AnActionEvent): List<AnAction> {
    return e.project?.let { prj ->
      val selected = e.getData(CommonDataKeys.VIRTUAL_FILE)
      selected?.let { file ->
        getLinkActions(prj, file)
      }
    } ?: emptyList()
  }

  private fun getLinkActions(project: Project, file: VirtualFile): List<AnAction> {
    return VirtualFileRepoCache.getInstance(project).findRepoForFileOrDir(file).map { repo ->
      val repoInfo = PerRepoInfoCache.getInstance(project).getInfo(repo)
      repoInfo.status.localBranch()?.let { branch ->
        val issueNavigation = IssueNavigationConfiguration.getInstance(project)
        val branchName = branch.name
        val links = issueNavigation.findIssueLinks(branchName)
        links.map { match ->
          val text = match.range.substring(branchName)
          OpenBranchIssueAction(text, match.targetUrl)
        }
      } ?: emptyList()
    }.orElseGet { emptyList() }
  }
}
