package zielu.gittoolbox.ui.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import zielu.gittoolbox.ResBundle
import zielu.gittoolbox.config.AppConfig
import zielu.gittoolbox.config.GitToolBoxConfig2
import zielu.gittoolbox.ui.blame.BlameUiService
import zielu.gittoolbox.util.GtUtil

internal class RefreshBlameAction : DumbAwareAction(ResBundle.message("refresh.blame.action")) {
  override fun actionPerformed(event: AnActionEvent) {
    val project = AnAction.getEventProject(event)
    project?.let { AppConfig.getConfig() }?.let { isBlameEnabled(it) }?.also {
      BlameUiService.getExistingInstance(project).ifPresent { it.refreshBlame() }
    }
  }

  override fun update(event: AnActionEvent) {
    super.update(event)
    val project = AnAction.getEventProject(event)
    project?.let { AppConfig.getConfig() }?.let { isBlameEnabled(it) }?.let {
      event.presentation.isEnabled = it
    }
    project?.let { GtUtil.hasGitVcs(it) }?.let {
      event.presentation.isVisible = it
    }
  }

  private fun isBlameEnabled(config: GitToolBoxConfig2): Boolean {
    return config.showBlameWidget || config.showEditorInlineBlame
  }
}
