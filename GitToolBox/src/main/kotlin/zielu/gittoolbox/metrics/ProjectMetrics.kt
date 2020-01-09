package zielu.gittoolbox.metrics

import com.intellij.openapi.project.Project
import zielu.gittoolbox.util.AppUtil.getServiceInstance

internal interface ProjectMetrics : Metrics {
  companion object {
    @JvmStatic
    fun getInstance(project: Project): Metrics {
      return getServiceInstance(project, ProjectMetrics::class.java)
    }
  }
}
