package zielu.gittoolbox.fetch

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import zielu.gittoolbox.util.AppUtil
import java.util.Optional
import java.util.concurrent.atomic.AtomicBoolean

internal class AutoFetchAllowedBuild(private val project: Project) {
  private val gateway = AutoFetchAllowedLocalGateway(project)
  private val buildRunning = AtomicBoolean()

  fun isFetchAllowed(): Boolean {
    return !buildRunning.get()
  }

  fun onBuildStarted(builtProject: Project) {
    log.debug("Build started")
    if (isCurrentProject(builtProject)) {
      buildRunning.set(true)
    }
  }

  fun onBuildFinished(builtProject: Project) {
    log.debug("Build finished")
    if (isCurrentProject(builtProject)) {
      if (buildRunning.compareAndSet(true, false)) {
        gateway.fireStateChanged()
      }
    }
  }

  private fun isCurrentProject(builtProject: Project): Boolean {
    return project == builtProject
  }

  companion object {
    private val log = Logger.getInstance(AutoFetchAllowedBuild::class.java)

    fun getInstance(project: Project): Optional<AutoFetchAllowedBuild> {
      return AppUtil.getServiceInstanceSafe(project, AutoFetchAllowedBuild::class.java)
    }
  }
}
