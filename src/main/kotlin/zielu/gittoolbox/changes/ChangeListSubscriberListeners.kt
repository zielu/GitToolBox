package zielu.gittoolbox.changes

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.ChangeList
import com.intellij.openapi.vcs.changes.ChangeListListener
import zielu.gittoolbox.config.AppConfigNotifier
import zielu.gittoolbox.config.GitToolBoxConfig2

internal class ChangeListSubscriberConfigListener(private val project: Project) : AppConfigNotifier {
  override fun configChanged(previous: GitToolBoxConfig2, current: GitToolBoxConfig2) {
    ChangeListSubscriber.getInstance(project).onConfigChanged(current)
  }
}

internal class ChangeListSubscriberChangeListListener(private val project: Project) : ChangeListListener {
  override fun changeListRemoved(list: ChangeList) {
    ChangeListSubscriber.getInstance(project).onChangeListRemoved(list)
  }

  override fun changeListUpdateDone() {
    ChangeListSubscriber.getInstance(project).onChangeListsUpdated()
  }
}
