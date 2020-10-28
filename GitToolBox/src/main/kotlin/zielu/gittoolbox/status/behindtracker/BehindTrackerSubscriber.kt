package zielu.gittoolbox.status.behindtracker

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import git4idea.repo.GitRepository
import zielu.gittoolbox.GitToolBoxApp
import zielu.gittoolbox.cache.RepoInfo
import zielu.gittoolbox.util.AppUtil
import zielu.gittoolbox.util.DisposeSafeRunnable
import zielu.gittoolbox.util.GtUtil
import zielu.gittoolbox.util.ReschedulingExecutor
import java.util.Optional
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

internal class BehindTrackerSubscriber(
  private val project: Project
) : Disposable {
  private val executor: ReschedulingExecutor = ReschedulingExecutor(
    { task, duration ->
      GitToolBoxApp.getInstance().flatMap {
        val result: Optional<Future<*>> = Optional.ofNullable(
          it.schedule(task, duration.toMillis(), TimeUnit.MILLISECONDS)
        )
        result
      }
    },
    true
  )

  fun onStateChanged(repoInfo: RepoInfo, repository: GitRepository) {
    if (log.isDebugEnabled) {
      log.debug("State changed [", GtUtil.name(repository), "]: ", repoInfo)
    }
    BehindTracker.getInstance(project).onStateChange(repository, repoInfo)
    scheduleNotifyTask()
  }

  private fun scheduleNotifyTask() {
    val task = BehindNotifyTask(project)
    executor.schedule("behind-notify", DisposeSafeRunnable(project, task), 10, TimeUnit.SECONDS)
  }

  override fun dispose() {
    executor.dispose()
  }

  companion object {
    private val log = Logger.getInstance(BehindTrackerSubscriber::class.java)

    fun getInstance(project: Project): BehindTrackerSubscriber {
      return AppUtil.getServiceInstance(project, BehindTrackerSubscriber::class.java)
    }
  }
}
