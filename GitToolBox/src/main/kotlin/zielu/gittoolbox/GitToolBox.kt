package zielu.gittoolbox

import com.intellij.ide.plugins.IdeaPluginDescriptor

object GitToolBox {
  const val PLUGIN_ID = "zielu.gittoolbox"

  fun isItMe(pluginDescriptor: IdeaPluginDescriptor): Boolean {
    return PLUGIN_ID == pluginDescriptor.pluginId.idString
  }
}
