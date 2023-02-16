package zielu.gittoolbox.config

import zielu.gittoolbox.util.BaseFacade

internal class AppConfigFacade : BaseFacade() {

  fun migrate(state: GitToolBoxConfig2): Boolean {
    return ConfigMigrator().migrate(state)
  }

  fun publishUpdated(previous: GitToolBoxConfig2, current: GitToolBoxConfig2) {
    publishAppSync { it.syncPublisher(AppConfigNotifier.CONFIG_TOPIC).configChanged(previous, current) }
  }
}
