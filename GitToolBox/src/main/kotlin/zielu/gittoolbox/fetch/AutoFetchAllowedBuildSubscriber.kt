package zielu.gittoolbox.fetch

import com.intellij.compiler.server.BuildManagerListener
import com.intellij.openapi.application.ApplicationManager

internal class AutoFetchAllowedBuildSubscriber {
  init {
    ApplicationManager.getApplication().messageBus.connect()
      .subscribe(BuildManagerListener.TOPIC, AutoFetchAllowedBuildListener())
  }
}
