package zielu.gittoolbox.fetch

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import java.util.concurrent.atomic.AtomicBoolean

internal class AutoFetchAllowedDumbModeSubscriber(private val project: Project) {
  private val subscribed = AtomicBoolean()

  fun subscribe(enteredDumbMode: () -> Unit, exitedDumbMode: () -> Unit) {
    if (subscribed.compareAndSet(false, true)) {
      project.messageBus.connect(project).subscribe(DumbService.DUMB_MODE, object : DumbService.DumbModeListener {
        override fun enteredDumbMode() {
          enteredDumbMode()
        }

        override fun exitDumbMode() {
          exitedDumbMode()
        }
      })
    } else {
      log.warn("Already subscribed")
    }
  }

  companion object {
    private val log = Logger.getInstance(AutoFetchAllowedDumbModeSubscriber::class.java)
  }
}
