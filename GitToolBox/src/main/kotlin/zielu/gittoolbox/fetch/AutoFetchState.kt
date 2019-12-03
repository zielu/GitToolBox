package zielu.gittoolbox.fetch

import com.intellij.openapi.components.BaseComponent
import com.intellij.openapi.project.Project
import zielu.gittoolbox.extension.autofetch.AUTO_FETCH_ALLOWED_TOPIC
import zielu.gittoolbox.extension.autofetch.AutoFetchAllowed
import zielu.gittoolbox.extension.autofetch.AutoFetchAllowedExtension
import zielu.gittoolbox.extension.autofetch.AutoFetchAllowedNotifier
import zielu.gittoolbox.util.AppUtil
import java.util.concurrent.atomic.AtomicBoolean

internal class AutoFetchState(private val project: Project) : BaseComponent {
  private val extension = AutoFetchAllowedExtension(project)
  private val fetchRunning = AtomicBoolean()

  override fun initComponent() {
    val connection = project.messageBus.connect(project)
    connection.subscribe(AUTO_FETCH_ALLOWED_TOPIC, object : AutoFetchAllowedNotifier {
      override fun stateChanged(allowed: AutoFetchAllowed) {
        fireStateChanged()
      }
    })
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
      return AppUtil.getComponent(project, AutoFetchState::class.java)
    }
  }
}
