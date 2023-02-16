package zielu.gittoolbox.config

import com.intellij.openapi.project.Project
import zielu.gittoolbox.util.PrjBaseFacade

internal class ProjectConfigFacade(
  private val project: Project
) : PrjBaseFacade(project) {

  fun migrate(appConfig: GitToolBoxConfig2, state: GitToolBoxConfigPrj): Boolean {
    val timer = getMetrics().timer("project-config.migrate")
    return timer.timeSupplierKt { ConfigMigrator().migrate(project, appConfig, state) }
  }

  fun publishUpdated(previous: GitToolBoxConfigPrj, current: GitToolBoxConfigPrj) {
    publishSync { it.syncPublisher(ProjectConfigNotifier.CONFIG_TOPIC).configChanged(previous, current) }
  }
}
