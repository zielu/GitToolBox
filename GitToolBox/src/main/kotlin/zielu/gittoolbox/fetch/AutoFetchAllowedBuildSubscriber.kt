package zielu.gittoolbox.fetch

import com.intellij.compiler.server.BuildManagerListener
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

internal class AutoFetchAllowedBuildSubscriber(private val project: Project) {
  private val subscribed = AtomicBoolean()

  fun subscribe(onBuildStarted: (project: Project) -> Unit, onBuildFinished: (project: Project) -> Unit) {
    if (subscribed.compareAndSet(false, true)) {
      project.messageBus.connect(project).subscribe(BuildManagerListener.TOPIC, object : BuildManagerListener {
        override fun buildStarted(project: Project, sessionId: UUID, isAutomake: Boolean) {
          onBuildStarted(project)
        }

        override fun buildFinished(project: Project, sessionId: UUID, isAutomake: Boolean) {
          onBuildFinished(project)
        }
      })
    } else {
      log.warn("Already subscribed")
    }
  }

  companion object {
    private val log = Logger.getInstance(AutoFetchAllowedBuildSubscriber::class.java)
  }
}
