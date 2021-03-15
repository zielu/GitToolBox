package zielu.gittoolbox.config

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.diagnostic.Logger
import com.intellij.serviceContainer.NonInjectable
import zielu.gittoolbox.util.AppUtil
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@State(name = "GitToolBoxAppSettings2", storages = [Storage("git_toolbox_2.xml")])
internal class AppConfig
@NonInjectable
constructor(
  private val facade: AppConfigFacade
) : PersistentStateComponent<GitToolBoxConfig2> {

  constructor() : this(AppConfigFacade())

  private val lock = ReentrantLock()
  private var state: GitToolBoxConfig2 = GitToolBoxConfig2()

  override fun getState(): GitToolBoxConfig2 {
    lock.withLock {
      return state
    }
  }

  override fun loadState(state: GitToolBoxConfig2) {
    lock.withLock {
      log.debug("App config state loaded: ", state)
      migrate(state)
      this.state = state
    }
  }

  private fun migrate(state: GitToolBoxConfig2) {
    val migrated = facade.migrate(state)
    if (migrated) {
      log.info("Migration done")
    } else {
      log.info("Already migrated")
    }
  }

  override fun noStateLoaded() {
    log.info("No persisted state of app configuration")
  }

  fun stateUpdated(before: GitToolBoxConfig2) {
    lock.withLock {
      log.info("Config updated")
      if (before != state) {
        log.info("Current different than previous")
        facade.publishUpdated(before, state)
      }
    }
  }

  companion object {
    private val log = Logger.getInstance(AppConfig::class.java)

    @JvmStatic
    fun getConfig(): GitToolBoxConfig2 {
      return getInstance().getState()
    }

    @JvmStatic
    fun getInstance(): AppConfig {
      return AppUtil.getServiceInstance(AppConfig::class.java)
    }
  }
}
