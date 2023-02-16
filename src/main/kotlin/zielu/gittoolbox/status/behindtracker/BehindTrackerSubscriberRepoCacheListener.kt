package zielu.gittoolbox.status.behindtracker

import git4idea.repo.GitRepository
import zielu.gittoolbox.cache.PerRepoStatusCacheListener
import zielu.gittoolbox.cache.RepoInfo

internal class BehindTrackerSubscriberRepoCacheListener : PerRepoStatusCacheListener {
  override fun stateChanged(info: RepoInfo, repository: GitRepository) {
    BehindTrackerSubscriber.getInstance(repository.project).onStateChanged(info, repository)
  }
}
