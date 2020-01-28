package zielu.gittoolbox.lifecycle

import com.intellij.ide.plugins.DynamicPluginListener
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import zielu.gittoolbox.GitToolBox

internal class PluginLifecycleListener : DynamicPluginListener {
  override fun pluginLoaded(pluginDescriptor: IdeaPluginDescriptor) {
    if (GitToolBox.isItMe(pluginDescriptor)) {
      log.info("Plugin loaded")
      // TODO will the platform run any startup activities ?
    }
  }

  override fun beforePluginUnload(pluginDescriptor: IdeaPluginDescriptor, isUpdate: Boolean) {
    if (GitToolBox.isItMe(pluginDescriptor)) {
      log.info("Plugin unloading")
      // TODO places to handle
      // zielu.gittoolbox.changes.ChangeListSubscriberLocalGateway.subscribe
      ApplicationManager.getApplication().messageBus.syncPublisher(PluginLifecycleNotifier.TOPIC).beforePluginUnload()
    }
  }

  private companion object {
    private val log = Logger.getInstance(PluginLifecycleListener::class.java)
  }
}
