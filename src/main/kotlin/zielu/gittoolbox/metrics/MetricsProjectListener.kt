package zielu.gittoolbox.metrics

import com.intellij.openapi.project.Project
import zielu.gittoolbox.lifecycle.ProjectLifecycleNotifier

internal class MetricsProjectListener : ProjectLifecycleNotifier {
  override fun projectReady(project: Project) {
    ProjectMetrics.startReporting(project)
  }
}
