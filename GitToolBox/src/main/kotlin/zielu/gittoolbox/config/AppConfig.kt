package zielu.gittoolbox.config

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.diagnostic.Logger
import zielu.gittoolbox.util.AppUtil

@State(name = "GitToolBoxAppSettings2", storages = [Storage("git_toolbox_2.xml")])
internal class AppConfig : PersistentStateComponent<GitToolBoxConfig2> {
  private var state: GitToolBoxConfig2 = GitToolBoxConfig2()

  override fun getState(): GitToolBoxConfig2 = state

  override fun loadState(state: GitToolBoxConfig2) {
    this.state = state
  }

  override fun initializeComponent() {
    val migrated = ConfigMigrator().migrate(state)
    if (migrated) {
      log.info("Migration done")
    } else {
      log.info("Already migrated")
    }
  }

  fun updateState(updated: GitToolBoxConfig2) {
    val current = state
    if (updated != current) {
      state = updated
      fireUpdated(current, updated)
    }
  }

  private fun fireUpdated(previous: GitToolBoxConfig2, current: GitToolBoxConfig2) {
    ApplicationManager.getApplication().messageBus
      .syncPublisher(AppConfigNotifier.CONFIG_TOPIC)
      .configChanged(previous, current)
  }

  companion object {
    private val log = Logger.getInstance(AppConfig::class.java)

    @JvmStatic
    fun getConfig(): GitToolBoxConfig2 {
      return getInstance().state
    }

    @JvmStatic
    fun getInstance(): AppConfig {
      return AppUtil.getServiceInstance(AppConfig::class.java)
    }
  }
}
