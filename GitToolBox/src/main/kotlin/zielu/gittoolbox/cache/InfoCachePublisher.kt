package zielu.gittoolbox.cache

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import git4idea.repo.GitRepository
import zielu.gittoolbox.cache.PerRepoInfoCache.CACHE_CHANGE
import zielu.gittoolbox.util.DisposeSafeRunnable

internal class InfoCachePublisher(private val project: Project) {
  private val messageBus by lazy {
    project.messageBus
  }

  fun notifyEvicted(repositories: Collection<GitRepository>) {
    runInBackground { messageBus.syncPublisher(CACHE_CHANGE).evicted(repositories) }
  }

  fun notifyRepoChanged(repo: GitRepository, previous: RepoInfo, current: RepoInfo) {
    runInBackground {
      messageBus.syncPublisher(CACHE_CHANGE).stateChanged(previous, current, repo)
      log.debug("Published cache changed event: ", repo)
    }
  }

  private fun runInBackground(task: () -> Unit) {
    ApplicationManager.getApplication().executeOnPooledThread(DisposeSafeRunnable(project, task))
  }

  private companion object {
    private val log = Logger.getInstance(InfoCachePublisher::class.java)
  }
}
