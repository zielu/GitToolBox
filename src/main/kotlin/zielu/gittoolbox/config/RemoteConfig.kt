package zielu.gittoolbox.config

internal data class RemoteConfig(var name: String = "") {
  fun copy(): RemoteConfig {
    return RemoteConfig(name)
  }
}
