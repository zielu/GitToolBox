package zielu.gittoolbox.store

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.project.Project
import zielu.gittoolbox.util.AppUtil

@State(name = "GitToolBoxStore", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
internal class WorkspaceStore : PersistentStateComponent<WorkspaceState> {
  private var state: WorkspaceState = WorkspaceState()

  override fun getState(): WorkspaceState = state

  override fun loadState(state: WorkspaceState) {
    this.state = state
  }

  companion object {
    @JvmStatic
    fun get(project: Project): WorkspaceState {
      return getInstance(project).state
    }

    @JvmStatic
    fun getInstance(project: Project): WorkspaceStore {
      return AppUtil.getServiceInstance(project, WorkspaceStore::class.java)
    }
  }
}
