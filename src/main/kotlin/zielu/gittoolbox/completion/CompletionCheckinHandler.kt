package zielu.gittoolbox.completion

import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.checkin.CheckinHandler
import com.intellij.ui.EditorTextComponent
import zielu.gittoolbox.metrics.ProjectMetrics
import java.io.File

internal class CompletionCheckinHandler(
  private val panel: CheckinProjectPanel
) : CheckinHandler(), CompletionScopeProvider {
  override fun getAffectedFiles(): Collection<File> {
    if (isCommitUiActive()) {
      val project = panel.project
      return ProjectMetrics.getInstance(project).timer("completion-get-affected")
        .timeSupplierKt { panel.files }
    }
    return emptyList()
  }

  private fun isCommitUiActive(): Boolean {
    val component = panel.preferredFocusedComponent
    return if (component is EditorTextComponent) {
      (component as EditorTextComponent).component.isFocusOwner
    } else false
  }
}
