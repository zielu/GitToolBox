package zielu.gittoolbox.cache

import git4idea.repo.GitRepository

internal interface PerRepoStatusCacheListener {
  @JvmDefault
  fun stateChanged(previous: RepoInfo, current: RepoInfo, repository: GitRepository) {
    stateChanged(current, repository)
  }

  @JvmDefault
  fun stateChanged(info: RepoInfo, repository: GitRepository) {}

  @JvmDefault
  fun evicted(repositories: Collection<GitRepository>) {}

  @JvmDefault
  fun allRepositoriesInitialized(repositories: Collection<GitRepository>) {}
}
