package zielu.gittoolbox.cache

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import git4idea.repo.GitRepository
import zielu.gittoolbox.cache.PerRepoInfoCache.CACHE_CHANGE
import zielu.gittoolbox.util.LocalGateway

internal class InfoCachePublisher(project: Project) : LocalGateway(project), Disposable {

  fun notifyEvicted(repositories: Collection<GitRepository>) {
    publishAsync(this) { it.syncPublisher(CACHE_CHANGE).evicted(repositories) }
  }

  fun notifyRepoChanged(repo: GitRepository, previous: RepoInfo, current: RepoInfo) {
    publishAsync(this) {
      it.syncPublisher(CACHE_CHANGE).stateChanged(previous, current, repo)
      log.debug("Published cache changed event: ", repo)
    }
  }

  override fun dispose() {
    // do nothing
  }

  private companion object {
    private val log = Logger.getInstance(InfoCachePublisher::class.java)
  }
}
