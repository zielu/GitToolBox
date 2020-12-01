package zielu.gittoolbox.fetch

import com.intellij.openapi.project.Project
import zielu.gittoolbox.extension.autofetch.AutoFetchAllowedExtension
import zielu.gittoolbox.util.AppUtil
import java.util.concurrent.atomic.AtomicBoolean

internal class AutoFetchState(private val project: Project) {
  private val extension = AutoFetchAllowedExtension(project)
  private val fetchRunning = AtomicBoolean()

  fun onAutoFetchAllowedChanged() {
    fireStateChanged()
  }

  private fun fireStateChanged() {
    project.messageBus.syncPublisher(AutoFetchNotifier.TOPIC).stateChanged(this)
  }

  fun canAutoFetch() = !fetchRunning.get() && extension.isFetchAllowed()

  fun fetchStart() = fetchRunning.compareAndSet(false, true)

  fun fetchFinish() {
    fetchRunning.compareAndSet(true, false)
  }

  companion object {
    @JvmStatic
    fun getInstance(project: Project): AutoFetchState {
      return AppUtil.getServiceInstance(project, AutoFetchState::class.java)
    }
  }
}
