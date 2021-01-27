package zielu.gittoolbox.cache

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import git4idea.repo.GitRepository
import zielu.gittoolbox.cache.PerRepoInfoCache.Companion.CACHE_CHANGE_TOPIC
import zielu.gittoolbox.util.LocalGateway

internal class InfoCachePublisher(project: Project) : LocalGateway(project), Disposable {

  fun notifyEvicted(repositories: Collection<GitRepository>) {
    publishAsync(this) { it.syncPublisher(CACHE_CHANGE_TOPIC).evicted(repositories) }
  }

  fun notifyRepoChanged(repo: GitRepository, previous: RepoInfo, current: RepoInfo) {
    publishAsync(this) {
      it.syncPublisher(CACHE_CHANGE_TOPIC).stateChanged(previous, current, repo)
      log.debug("Published cache changed event: ", repo)
    }
  }

  fun notifyAllRepositoriesInitialized(repositories: Collection<GitRepository>) {
    publishAsync(this) {
      it.syncPublisher(CACHE_CHANGE_TOPIC).allRepositoriesInitialized(repositories)
      log.debug("Publish all repositories initialized: ", repositories)
    }
  }

  override fun dispose() {
    // do nothing
  }

  private companion object {
    private val log = Logger.getInstance(InfoCachePublisher::class.java)
  }
}
