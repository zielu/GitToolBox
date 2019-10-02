package zielu.gittoolbox.fetch

import git4idea.repo.GitRemote
import git4idea.repo.GitRepository
import zielu.gittoolbox.config.AutoFetchExclusionConfig

internal class RemoteFilteredRepository(val repo: GitRepository,
                                        val config: AutoFetchExclusionConfig) : GitRepository by repo {
    override fun getRemotes(): Collection<GitRemote> {
        return repo.remotes.filter { !config.isRemoteExcluded(it.name) }
    }
}