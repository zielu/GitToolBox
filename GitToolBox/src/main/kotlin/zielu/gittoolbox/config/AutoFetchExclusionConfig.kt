package zielu.gittoolbox.config

data class AutoFetchExclusionConfig(var repositoryRootPath: String = "") {
    var excludedRemotes: MutableList<RemoteConfig> = ArrayList()

    fun copy(): AutoFetchExclusionConfig {
        val copy = AutoFetchExclusionConfig(repositoryRootPath)
        copy.excludedRemotes = ArrayList(excludedRemotes.map { it.copy() })
        return copy
    }

    fun isRemoteExcluded(remoteName: String): Boolean {
        return excludedRemotes.any { remoteName == it.name }
    }

    fun noRemotes(): Boolean {
        return excludedRemotes.isEmpty();
    }

    fun remoteRemote(remote: RemoteConfig): Boolean {
        return excludedRemotes.remove(remote)
    }
}
