package zielu.gittoolbox.cache

import git4idea.repo.GitRepository

internal interface DirMappingAware {
  @JvmSuppressWildcards(true)
  fun updatedRepoList(repositories: List<GitRepository>)
}
