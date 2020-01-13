package zielu.gittoolbox.changes

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import zielu.gittoolbox.config.AppConfigNotifier
import zielu.gittoolbox.config.GitToolBoxConfig2

internal class ChangeListSubscriberConfigListener(private val project: Project) : AppConfigNotifier {
  override fun configChanged(previous: GitToolBoxConfig2, current: GitToolBoxConfig2) {
    ChangeListSubscriber.getInstance(project).onConfigChanged(current)
  }
}

internal class ChangeListSubscribeProjectListener : ProjectManagerListener {
  override fun projectOpened(project: Project) {
    if (!project.isDefault) {
      ChangeListSubscriber.getInstance(project).onProjectOpened()
    }
  }
}
