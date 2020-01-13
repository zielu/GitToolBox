package zielu.gittoolbox.fetch

import com.intellij.openapi.project.Project
import zielu.gittoolbox.extension.autofetch.AutoFetchAllowed
import zielu.gittoolbox.extension.autofetch.AutoFetchAllowedNotifier

internal class AutoFetchStateAllowedListener(private val project: Project) : AutoFetchAllowedNotifier {
  override fun stateChanged(allowed: AutoFetchAllowed) {
    AutoFetchState.getInstance(project).onAutoFetchAllowedChanged()
  }
}
