package zielu.gittoolbox.cache

import git4idea.repo.GitRepository
import git4idea.repo.GitRepositoryChangeListener

internal class CacheSourcesSubscriberGitRepositoryListener : GitRepositoryChangeListener {
  override fun repositoryChanged(repository: GitRepository) {
    CacheSourcesSubscriber.getInstance(repository.project).onRepoChanged(repository)
  }
}
