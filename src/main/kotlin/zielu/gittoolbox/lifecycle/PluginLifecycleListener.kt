package zielu.gittoolbox.lifecycle

import com.intellij.ide.plugins.DynamicPluginListener
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.openapi.diagnostic.Logger
import zielu.gittoolbox.GitToolBox

internal class PluginLifecycleListener : DynamicPluginListener {
  override fun pluginLoaded(pluginDescriptor: IdeaPluginDescriptor) {
    if (GitToolBox.isItMe(pluginDescriptor)) {
      log.info("Plugin loaded")
    }
  }

  override fun beforePluginUnload(pluginDescriptor: IdeaPluginDescriptor, isUpdate: Boolean) {
    if (GitToolBox.isItMe(pluginDescriptor)) {
      log.info("Plugin unloading started")
    }
  }

  private companion object {
    private val log = Logger.getInstance(PluginLifecycleListener::class.java)
  }
}
