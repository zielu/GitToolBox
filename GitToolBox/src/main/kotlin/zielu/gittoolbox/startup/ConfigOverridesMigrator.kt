package zielu.gittoolbox.startup

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import zielu.gittoolbox.config.BoolConfigOverride
import zielu.gittoolbox.config.ExtrasConfig
import zielu.gittoolbox.config.GitToolBoxConfigPrj

internal class ConfigOverridesMigrator(
  private val project: Project,
  private val override: ExtrasConfig
) {

  fun migrate(prjConfig: GitToolBoxConfigPrj): Boolean {
    var migrated = false
    migrated = apply(override.autoFetchEnabledOverride, prjConfig::autoFetch) { v ->
      prjConfig.autoFetch = v
    } || migrated
    migrated = apply(override.autoFetchOnBranchSwitchOverride, prjConfig::autoFetchOnBranchSwitch) { v ->
      prjConfig.autoFetchOnBranchSwitch = v
    } || migrated
    log.debug("Project overrides migration status ", migrated, " for ", project)
    return migrated
  }

  private fun apply(override: BoolConfigOverride, getValue: () -> Boolean, setValue: (Boolean) -> Unit): Boolean {
    if (override.enabled && override.isNotApplied(project)) {
      if (getValue.invoke() != override.value) {
        setValue.invoke(override.value)
        override.applied(project)
        return true
      }
    }
    return false
  }

  private companion object {
    private val log: Logger = Logger.getInstance(ConfigOverridesMigrator::class.java)
  }
}
