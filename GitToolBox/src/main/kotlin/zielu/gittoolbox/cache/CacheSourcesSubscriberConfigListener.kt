package zielu.gittoolbox.cache

import com.intellij.openapi.project.Project
import zielu.gittoolbox.config.GitToolBoxConfigPrj
import zielu.gittoolbox.config.ProjectConfigNotifier

internal class CacheSourcesSubscriberConfigListener(private val project: Project) : ProjectConfigNotifier {
  override fun configChanged(previous: GitToolBoxConfigPrj, current: GitToolBoxConfigPrj) {
    CacheSourcesSubscriber.getInstance(project).onConfigChanged(previous, current)
  }
}
