package zielu.gittoolbox.config

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.serviceContainer.NonInjectable
import zielu.gittoolbox.GitToolBoxRegistry
import zielu.gittoolbox.util.AppUtil
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@State(name = "GitToolBoxProjectSettings", storages = [Storage("git_toolbox_prj.xml")])
internal class ProjectConfig
@NonInjectable
constructor(
  private val facade: ProjectConfigFacade,
) : PersistentStateComponent<GitToolBoxConfigPrj> {
  private val lock = ReentrantLock()
  private var state: GitToolBoxConfigPrj = GitToolBoxConfigPrj()

  constructor(project: Project) : this(ProjectConfigFacade(project))

  override fun getState(): GitToolBoxConfigPrj {
    lock.withLock {
      return state
    }
  }

  override fun loadState(state: GitToolBoxConfigPrj) {
    lock.withLock {
      log.debug("Project config state loaded: ", state)
      migrate(state)
      this.state = state
    }
  }

  override fun noStateLoaded() {
    log.info("No persisted state of project configuration")
  }

  private fun migrate(state: GitToolBoxConfigPrj) {
    val result = facade.migrate(AppConfig.getConfig(), state)
    if (result) {
      log.info("Migration done")
    } else {
      log.info("Already migrated")
    }
  }

  fun stateUpdated(before: GitToolBoxConfigPrj) {
    lock.withLock {
      if (before != state) {
        facade.publishUpdated(before, state)
      }
    }
  }

  companion object {
    private val log = Logger.getInstance(ProjectConfig::class.java)

    @JvmStatic
    fun getConfig(project: Project): GitToolBoxConfigPrj {
      return getInstance(project).getState()
    }

    @JvmStatic
    fun getMerged(project: Project): MergedProjectConfig {
      return MergedProjectConfig(AppConfig.getConfig(), getConfig(project), GitToolBoxRegistry.useLegacyConfig())
    }

    @JvmStatic
    fun getMerged(config: GitToolBoxConfigPrj): MergedProjectConfig {
      return MergedProjectConfig(AppConfig.getConfig(), config, GitToolBoxRegistry.useLegacyConfig())
    }

    @JvmStatic
    fun getMerged(appConfig: GitToolBoxConfig2, project: Project): MergedProjectConfig {
      return MergedProjectConfig(AppConfig.getConfig(), getConfig(project), GitToolBoxRegistry.useLegacyConfig())
    }

    @JvmStatic
    fun getInstance(project: Project): ProjectConfig {
      return AppUtil.getServiceInstance(project, ProjectConfig::class.java)
    }
  }
}
