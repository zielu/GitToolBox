package zielu.gittoolbox.store

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import zielu.gittoolbox.util.AppUtil
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@State(name = "GitToolBoxStore", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
internal class WorkspaceStore : PersistentStateComponent<WorkspaceState> {
  private val lock = ReentrantLock()
  private var state: WorkspaceState = WorkspaceState()

  override fun getState(): WorkspaceState {
    lock.withLock {
      return state
    }
  }

  override fun loadState(state: WorkspaceState) {
    log.debug("Project workspace state loaded: ", state)
    lock.withLock {
      this.state = state
    }
  }

  companion object {
    private val log = Logger.getInstance(WorkspaceStore::class.java)

    @JvmStatic
    fun get(project: Project): WorkspaceState {
      return getInstance(project).getState()
    }

    @JvmStatic
    fun getInstance(project: Project): WorkspaceStore {
      return AppUtil.getServiceInstance(project, WorkspaceStore::class.java)
    }
  }
}
