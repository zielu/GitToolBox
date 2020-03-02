package zielu.gittoolbox.store

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.Transient
import zielu.gittoolbox.util.AppUtil

@State(name = "GitToolBoxStore", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
internal data class WorkspaceStore(
  var recentBranches: RecentBranches = RecentBranches()
) : PersistentStateComponent<WorkspaceStore> {

  @Transient
  fun copy(): WorkspaceStore {
    return WorkspaceStore(
      recentBranches.copy()
    )
  }

  override fun getState(): WorkspaceStore = this

  override fun loadState(state: WorkspaceStore) {
    XmlSerializerUtil.copyBean(state, this)
  }

  companion object {
    fun getInstance(project: Project): WorkspaceStore {
      return AppUtil.getServiceInstance(project, WorkspaceStore::class.java)
    }
  }
}
