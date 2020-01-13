package zielu.gittoolbox.config

import com.intellij.util.messages.Topic

internal interface AppConfigNotifier {
  fun configChanged(previous: GitToolBoxConfig2, current: GitToolBoxConfig2)

  companion object {
    @JvmField
    val CONFIG_TOPIC = Topic.create("Git ToolBox Config", AppConfigNotifier::class.java)
  }
}
