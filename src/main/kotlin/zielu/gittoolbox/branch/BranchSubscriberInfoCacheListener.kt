package zielu.gittoolbox.branch

import git4idea.repo.GitRepository
import zielu.gittoolbox.cache.PerRepoStatusCacheListener
import zielu.gittoolbox.cache.RepoInfo

internal class BranchSubscriberInfoCacheListener : PerRepoStatusCacheListener {
  override fun stateChanged(previous: RepoInfo, current: RepoInfo, repository: GitRepository) {
    BranchSubscriber.getInstance(repository.project).onRepoStateChanged(previous, current, repository)
  }
}
