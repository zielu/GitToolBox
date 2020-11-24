package zielu.gittoolbox.config

import com.intellij.util.messages.Topic

internal interface ProjectConfigNotifier {
  fun configChanged(previous: GitToolBoxConfigPrj, current: GitToolBoxConfigPrj)

  companion object {
    @JvmField
    val CONFIG_TOPIC = Topic.create("Git ToolBox Project Config", ProjectConfigNotifier::class.java)
  }
}
