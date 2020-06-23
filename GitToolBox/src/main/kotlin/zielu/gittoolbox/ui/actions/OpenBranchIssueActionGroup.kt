package zielu.gittoolbox.ui.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.vcs.IssueNavigationConfiguration
import zielu.gittoolbox.cache.PerRepoInfoCache
import zielu.gittoolbox.cache.VirtualFileRepoCache

internal class OpenBranchIssueActionGroup : GitToolboxActionGroup() {
    override fun getChildren(e: AnActionEvent?): Array<out AnAction> {
        return e?.let {
            return getActions(it)
        } ?: emptyArray()
    }

    private fun getActions(e: AnActionEvent): Array<out AnAction> {
        return e.project?.let { prj ->
            val selected = e.getData(CommonDataKeys.VIRTUAL_FILE)
            selected?.let { file ->
                VirtualFileRepoCache.getInstance(prj).findRepoForFileOrDir(file).map { repo ->
                    val repoInfo = PerRepoInfoCache.getInstance(prj).getInfo(repo)
                    repoInfo.status().localBranch()?.let { branch ->
                        val issueNavigation = IssueNavigationConfiguration.getInstance(prj)
                        val branchName = branch.name
                        val links = issueNavigation.findIssueLinks(branchName)
                        links.map { match ->
                            val text = match.range.substring(branchName)
                            OpenBranchIssueAction(text, match.targetUrl)
                        }.toTypedArray()
                    } ?: emptyArray()
                }.orElseGet { emptyArray() }
            }
        } ?: emptyArray()
    }
}
