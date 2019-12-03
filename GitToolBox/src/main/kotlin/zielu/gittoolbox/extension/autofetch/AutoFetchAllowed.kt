package zielu.gittoolbox.extension.autofetch

import com.intellij.util.messages.Topic

internal interface AutoFetchAllowed {
  fun initialize()

  fun isAllowed(): Boolean
}

internal interface AutoFetchAllowedNotifier {
  fun stateChanged(allowed: AutoFetchAllowed)
}

internal val AUTO_FETCH_ALLOWED_TOPIC: Topic<AutoFetchAllowedNotifier> = Topic.create(
  "Git ToolBox Auto Fetch Allowed",
  AutoFetchAllowedNotifier::class.java
)
