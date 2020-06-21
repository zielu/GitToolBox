package zielu.gittoolbox.store

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.project.Project
import com.intellij.serviceContainer.NonInjectable
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.Transient
import zielu.gittoolbox.util.AppUtil

@State(name = "GitToolBoxStore", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
internal data class WorkspaceStore

@NonInjectable
constructor(
  var recentBranches: RecentBranches = RecentBranches(),
  var projectConfigVersion: Int = 1
) : PersistentStateComponent<WorkspaceStore> {

  @Transient
  fun copy(): WorkspaceStore {
    return WorkspaceStore(
      recentBranches.copy(),
      projectConfigVersion
    )
  }

  override fun getState(): WorkspaceStore? = this

  override fun loadState(state: WorkspaceStore) {
    XmlSerializerUtil.copyBean(state, this)
  }

  companion object {
    @JvmStatic
    fun getInstance(project: Project): WorkspaceStore {
      return AppUtil.getServiceInstance(project, WorkspaceStore::class.java)
    }
  }
}
