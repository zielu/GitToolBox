package zielu.gittoolbox.fetch

import com.intellij.util.messages.Topic

internal interface AutoFetchNotifier {
  fun stateChanged(state: AutoFetchState)

  companion object {
    val TOPIC = Topic.create("Git ToolBox Auto Fetch", AutoFetchNotifier::class.java)
  }
}
