package zielu.gittoolbox.changes

import com.intellij.openapi.project.Project
import zielu.gittoolbox.config.AppConfigNotifier
import zielu.gittoolbox.config.GitToolBoxConfig2
import zielu.gittoolbox.lifecycle.ProjectLifecycleNotifier

internal class ChangeListSubscriberConfigListener(private val project: Project) : AppConfigNotifier {
  override fun configChanged(previous: GitToolBoxConfig2, current: GitToolBoxConfig2) {
    ChangeListSubscriber.getInstance(project).onConfigChanged(current)
  }
}

internal class ChangeListSubscribeProjectListener : ProjectLifecycleNotifier {
  override fun projectReady(project: Project) {
    ChangeListSubscriber.getInstance(project).onProjectReady()
  }
}
