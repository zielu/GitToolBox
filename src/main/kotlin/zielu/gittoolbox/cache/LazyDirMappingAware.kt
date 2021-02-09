package zielu.gittoolbox.cache

import git4idea.repo.GitRepository

internal class LazyDirMappingAware<out T : DirMappingAware> (
  supplier: () -> T
) : DirMappingAware {
  private val delegate: T by lazy {
    supplier.invoke()
  }

  override fun updatedRepoList(repositories: List<GitRepository>) {
    delegate.updatedRepoList(repositories)
  }
}
