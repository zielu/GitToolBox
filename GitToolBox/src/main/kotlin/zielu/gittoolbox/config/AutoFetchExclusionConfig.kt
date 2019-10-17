package zielu.gittoolbox.config

import com.intellij.util.xmlb.annotations.Transient

data class AutoFetchExclusionConfig(
  var repositoryRootPath: String = "",
  var excludedRemotes: MutableList<RemoteConfig> = ArrayList()
) {

  constructor(repositoryRootPath: String) : this(repositoryRootPath, ArrayList())

  fun copy(): AutoFetchExclusionConfig {
    return AutoFetchExclusionConfig(repositoryRootPath,
        ArrayList(excludedRemotes.map { it.copy() }))
  }

  fun isRemoteExcluded(remoteName: String): Boolean {
    return excludedRemotes.any { remoteName == it.name }
  }

  fun remoteRemote(remote: RemoteConfig): Boolean {
    return excludedRemotes.remove(remote)
  }

  fun noRemotes(): Boolean {
    return excludedRemotes.isEmpty()
  }

  @Transient
  fun hasRemotes(): Boolean {
    return !noRemotes()
  }
}
