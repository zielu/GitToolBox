package zielu.gittoolbox.fetch

import com.intellij.openapi.project.Project
import org.slf4j.LoggerFactory
import zielu.gittoolbox.extension.autofetch.AutoFetchAllowed
import java.util.concurrent.atomic.AtomicBoolean

internal class AutoFetchAllowedBuild(private val project: Project) : AutoFetchAllowed {
  private val gateway = AutoFetchAllowedLocalGateway(project)
  private val subscriber = AutoFetchAllowedBuildSubscriber(project)
  private val buildRunning = AtomicBoolean()

  override fun initialize() {
    subscriber.subscribe(this::onBuildStarted, this::onBuildFinished)
  }

  private fun onBuildStarted(builtProject: Project) {
    log.debug("Build started")
    if (isCurrentProject(builtProject)) {
      buildRunning.set(true)
    }
  }

  private fun onBuildFinished(builtProject: Project) {
    log.debug("Build finished")
    if (isCurrentProject(builtProject)) {
      buildRunning.set(false)
      gateway.fireStateChanged(this)
    }
  }

  private fun isCurrentProject(builtProject: Project): Boolean {
    return project == builtProject
  }

  override fun isAllowed(): Boolean {
    return !buildRunning.get()
  }

  companion object {
    private val log = LoggerFactory.getLogger(AutoFetchAllowedBuild::class.java)
  }
}
