package zielu.gittoolbox.config

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.serviceContainer.NonInjectable
import com.intellij.util.xmlb.XmlSerializerUtil
import zielu.gittoolbox.util.AppUtil

@State(name = "GitToolBoxAppExtras", storages = [Storage("git_toolbox_extras.xml")])
internal data class GitToolBoxConfigExtras

@NonInjectable
constructor(
  var autoFetchEnabledOverride: BoolConfigOverride = BoolConfigOverride(),
  var autoFetchOnBranchSwitchOverride: BoolConfigOverride = BoolConfigOverride()
) : PersistentStateComponent<GitToolBoxConfigExtras> {

  fun copy(): GitToolBoxConfigExtras {
    return GitToolBoxConfigExtras(
      autoFetchEnabledOverride.copy(),
      autoFetchOnBranchSwitchOverride.copy()
    )
  }

  override fun getState(): GitToolBoxConfigExtras = this

  override fun loadState(state: GitToolBoxConfigExtras) {
    XmlSerializerUtil.copyBean(state, this)
  }

  companion object {
    @JvmStatic
    fun getInstance(): GitToolBoxConfigExtras {
      return AppUtil.getServiceInstance(GitToolBoxConfigExtras::class.java)
    }
  }
}
