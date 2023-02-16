package zielu.gittoolbox.fetch

import com.intellij.openapi.project.Project
import zielu.gittoolbox.extension.autofetch.AutoFetchAllowedNotifier

internal class AutoFetchStateAllowedListener(private val project: Project) : AutoFetchAllowedNotifier {
  override fun stateChanged() {
    AutoFetchState.getInstance(project).onAutoFetchAllowedChanged()
  }
}
