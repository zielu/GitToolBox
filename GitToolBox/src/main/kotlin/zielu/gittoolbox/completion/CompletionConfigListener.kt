package zielu.gittoolbox.completion

import com.intellij.openapi.project.Project
import zielu.gittoolbox.config.GitToolBoxConfigPrj
import zielu.gittoolbox.config.ProjectConfigNotifier

internal class CompletionConfigListener(private val project: Project) : ProjectConfigNotifier {
  override fun configChanged(previous: GitToolBoxConfigPrj, current: GitToolBoxConfigPrj) {
    CompletionService.getExistingInstance(project).ifPresent {
      service: CompletionService -> service.onConfigChanged(current)
    }
  }
}
