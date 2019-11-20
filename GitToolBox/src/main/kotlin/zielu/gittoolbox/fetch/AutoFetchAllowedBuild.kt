package zielu.gittoolbox.fetch

import com.intellij.compiler.server.BuildManagerListener
import com.intellij.openapi.project.Project
import org.slf4j.LoggerFactory
import zielu.gittoolbox.extension.AutoFetchAllowed
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

internal class AutoFetchAllowedBuild(private val project: Project) : AutoFetchAllowed {
  private val gateway = AutoFetchAllowedLocalGateway(project)
  private val buildNotRunning = AtomicBoolean(true)
  private val initialized = AtomicBoolean()

  override fun initialize() {
    if (initialized.compareAndSet(false, true)) {
      connectMessageBus()
    }
  }

  private fun connectMessageBus() {
    project.messageBus.connect(project).subscribe(BuildManagerListener.TOPIC, object : BuildManagerListener {
      override fun buildStarted(project: Project, sessionId: UUID, isAutomake: Boolean) {
        onBuildStarted(project)
      }

      override fun buildFinished(project: Project, sessionId: UUID, isAutomake: Boolean) {
        onBuildFinished(project)
      }
    })
  }

  private fun onBuildStarted(builtProject: Project) {
    log.debug("Build started")
    if (isCurrentProject(builtProject)) {
      buildNotRunning.set(false)
    }
  }

  private fun isCurrentProject(builtProject: Project): Boolean {
    return project == builtProject
  }

  private fun onBuildFinished(builtProject: Project) {
    log.debug("Build finished")
    if (isCurrentProject(builtProject)) {
      buildNotRunning.set(true)
      gateway.fireStateChanged(this)
    }
  }

  override fun isAllowed(): Boolean {
    return buildNotRunning.get()
  }

  companion object {
    private val log = LoggerFactory.getLogger(AutoFetchAllowedBuild::class.java)
  }
}
