package zielu.gittoolbox.config

data class RemoteConfig(var name: String = "") {

    fun copy(): RemoteConfig {
        return RemoteConfig(name)
    }
}
