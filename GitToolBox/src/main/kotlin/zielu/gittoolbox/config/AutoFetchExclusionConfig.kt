package zielu.gittoolbox.config

data class AutoFetchExclusionConfig(var repositoryRootPath: String = "") {
    var excludedRemotes: Set<RemoteConfig> = LinkedHashSet()

    fun copy(): AutoFetchExclusionConfig {
        val copy = AutoFetchExclusionConfig(repositoryRootPath)
        copy.excludedRemotes = LinkedHashSet(excludedRemotes.map { it.copy() })
        return copy
    }

    fun isRemoteExcluded(remoteName: String): Boolean {
        return excludedRemotes.any { remoteName == it.name }
    }

    fun noRemotes(): Boolean {
        return excludedRemotes.isEmpty();
    }
}