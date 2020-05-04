package zielu.gittoolbox.lifecycle

import com.intellij.util.messages.Topic

internal interface PluginLifecycleNotifier {
  fun pluginLoaded() {
    // default implementation
  }

  fun beforePluginUnload() {
    // default implementation
  }

  companion object {
    val TOPIC = Topic.create("Git ToolBox Plugin Lifecycle", PluginLifecycleNotifier::class.java)
  }
}
