package zielu.gittoolbox.fetch

import com.intellij.compiler.server.BuildManagerListener
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.messages.MessageBusConnection
import zielu.gittoolbox.util.AppUtil
import java.util.concurrent.atomic.AtomicBoolean

internal class AutoFetchAllowedBuildSubscriber : Disposable {
  private val initialized = AtomicBoolean()
  private var connection: MessageBusConnection? = null

  fun onAppInitialized() {
    if (initialized.compareAndSet(false, true)) {
      connection = ApplicationManager.getApplication().messageBus.connect(this)
      connection?.subscribe(BuildManagerListener.TOPIC, AutoFetchAllowedBuildListener())
    }
  }

  override fun dispose() {
    connection = null
  }

  companion object {
    fun getInstance(): AutoFetchAllowedBuildSubscriber {
      return AppUtil.getServiceInstance(AutoFetchAllowedBuildSubscriber::class.java)
    }
  }
}
