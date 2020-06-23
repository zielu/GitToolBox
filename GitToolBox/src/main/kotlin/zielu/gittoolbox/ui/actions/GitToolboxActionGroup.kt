package zielu.gittoolbox.ui.actions

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.AbstractVcs
import com.intellij.openapi.vcs.actions.StandardVcsGroup
import git4idea.GitVcs

internal open class GitToolboxActionGroup : StandardVcsGroup() {
    override fun getVcs(project: Project): AbstractVcs {
        return GitVcs.getInstance(project)
    }

    override fun getVcsName(project: Project): String {
        return GitVcs.NAME
    }
}
