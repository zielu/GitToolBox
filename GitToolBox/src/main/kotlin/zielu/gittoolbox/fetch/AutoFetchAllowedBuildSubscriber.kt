package zielu.gittoolbox.fetch

import com.intellij.compiler.server.BuildManagerListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.BaseComponent

internal class AutoFetchAllowedBuildSubscriber : BaseComponent {
  override fun initComponent() {
    ApplicationManager.getApplication().messageBus.connect()
      .subscribe(BuildManagerListener.TOPIC, AutoFetchAllowedBuildListener())
  }
}
