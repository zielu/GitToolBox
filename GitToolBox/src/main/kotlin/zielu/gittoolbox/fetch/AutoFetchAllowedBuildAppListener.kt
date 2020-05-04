package zielu.gittoolbox.fetch

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity

internal class AutoFetchAllowedBuildAppListener : StartupActivity {
  override fun runActivity(project: Project) {
    if (!project.isDefault) {
      AutoFetchAllowedBuildSubscriber.getInstance().onAppInitialized()
    }
  }
}
