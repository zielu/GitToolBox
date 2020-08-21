package zielu.gittoolbox.fetch

import com.intellij.compiler.server.BuildManagerListener
import com.intellij.openapi.project.Project
import java.util.UUID

internal class AutoFetchAllowedBuildListener : BuildManagerListener {
  override fun buildStarted(project: Project, sessionId: UUID, isAutomake: Boolean) {
    if (isNotDefault(project)) {
      AutoFetchAllowedBuild.getInstance(project)
        .ifPresent { it.onBuildStarted(project) }
    }
  }

  override fun buildFinished(project: Project, sessionId: UUID, isAutomake: Boolean) {
    if (isNotDefault(project)) {
      AutoFetchAllowedBuild.getInstance(project)
        .ifPresent { it.onBuildFinished(project) }
    }
  }

  private fun isNotDefault(project: Project): Boolean = !project.isDefault
}
