package zielu.gittoolbox.config

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import zielu.gittoolbox.config.override.BoolConfigOverride
import zielu.gittoolbox.util.AppUtil

@State(name = "GitToolBoxAppOverrides", storages = [Storage("git_toolbox_overrides.xml")])
internal data class GitToolBoxConfigOverride(
  var autoFetchEnabledOverride: BoolConfigOverride = BoolConfigOverride(),
  var autoFetchOnBranchSwitchOverride: BoolConfigOverride = BoolConfigOverride()
) : PersistentStateComponent<GitToolBoxConfigOverride> {

  fun copy(): GitToolBoxConfigOverride {
    return GitToolBoxConfigOverride(
      autoFetchEnabledOverride.copy(),
      autoFetchOnBranchSwitchOverride.copy()
    )
  }

  override fun getState(): GitToolBoxConfigOverride = this

  override fun loadState(state: GitToolBoxConfigOverride) {
    XmlSerializerUtil.copyBean(state, this)
  }

  companion object {
    @JvmStatic
    fun getInstance(): GitToolBoxConfigOverride {
      return AppUtil.getServiceInstance(GitToolBoxConfigOverride::class.java)
    }
  }
}
