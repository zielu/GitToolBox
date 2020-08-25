package zielu.gittoolbox.metrics

import com.intellij.openapi.project.Project
import zielu.gittoolbox.util.AppUtil.getServiceInstance
import zielu.gittoolbox.util.AppUtil.getServiceInstanceSafe

internal interface ProjectMetrics : Metrics {
  fun startReporting()

  companion object {
    @JvmStatic
    fun getInstance(project: Project): Metrics {
      return getServiceInstance(project, ProjectMetrics::class.java)
    }

    fun startReporting(project: Project) {
      getServiceInstanceSafe(project, ProjectMetrics::class.java).ifPresent { it.startReporting() }
    }
  }
}
