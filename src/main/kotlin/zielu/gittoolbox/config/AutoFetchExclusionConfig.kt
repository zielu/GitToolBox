package zielu.gittoolbox.config

import com.intellij.util.xmlb.annotations.Transient

internal data class AutoFetchExclusionConfig(
  var repositoryRootPath: String = "",
  var excludedRemotes: MutableList<RemoteConfig> = ArrayList()
) {

  constructor(repositoryRootPath: String) : this(repositoryRootPath, ArrayList())

  @Transient
  fun copy(): AutoFetchExclusionConfig {
    return AutoFetchExclusionConfig(repositoryRootPath, ArrayList(excludedRemotes.map { it.copy() }))
  }

  fun isRemoteExcluded(remoteName: String): Boolean {
    return excludedRemotes.any { remoteName == it.name }
  }

  @Transient
  fun noRemotes(): Boolean {
    return excludedRemotes.isEmpty()
  }

  @Transient
  fun hasRemotes(): Boolean {
    return excludedRemotes.isNotEmpty()
  }

  fun removeRemote(remote: RemoteConfig): Boolean {
    return excludedRemotes.remove(remote)
  }
}
