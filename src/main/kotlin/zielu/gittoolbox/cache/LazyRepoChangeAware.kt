package zielu.gittoolbox.cache

import git4idea.repo.GitRepository

internal class LazyRepoChangeAware<out T : RepoChangeAware> (
  supplier: () -> T
) : RepoChangeAware {
  private val delegate: T by lazy {
    supplier.invoke()
  }

  override fun repoChanged(repository: GitRepository) {
    delegate.repoChanged(repository)
  }
}
