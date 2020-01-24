package zielu.gittoolbox.lifecycle

import com.intellij.util.messages.Topic

internal interface PluginLifecycleNotifier {
  fun beforePluginUnload()

  companion object {
    val TOPIC = Topic.create("Git ToolBox Plugin Lifecycle", PluginLifecycleNotifier::class.java)
  }
}
