package zielu.gittoolbox.extension.autofetch

import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic

internal interface AutoFetchAllowed {
  fun isAllowed(project: Project): Boolean
}

internal interface AutoFetchAllowedNotifier {
  fun stateChanged()
}

internal val AUTO_FETCH_ALLOWED_TOPIC: Topic<AutoFetchAllowedNotifier> = Topic.create(
  "Git ToolBox Auto Fetch Allowed",
  AutoFetchAllowedNotifier::class.java
)
