package zielu.gittoolbox.startup

import com.intellij.openapi.project.Project
import zielu.gittoolbox.config.GitToolBoxConfigOverride
import zielu.gittoolbox.config.GitToolBoxConfigPrj

internal class ConfigOverridesMigrator(
  private val project: Project,
  private val override: GitToolBoxConfigOverride
) {

  fun migrate(prjConfig: GitToolBoxConfigPrj): Boolean {
    var migrated = false
    val autoFetchEnabledOverride = override.autoFetchEnabledOverride
    if (autoFetchEnabledOverride.enabled && autoFetchEnabledOverride.isNotApplied(project)) {
      if (prjConfig.autoFetch != autoFetchEnabledOverride.value) {
        prjConfig.autoFetch = autoFetchEnabledOverride.value
        autoFetchEnabledOverride.applied(project)
        migrated = true
      }
    }
    return migrated
  }
}
