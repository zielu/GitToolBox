package zielu.gittoolbox.status.behindtracker

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.util.concurrency.AppExecutorUtil
import git4idea.repo.GitRepository
import zielu.gittoolbox.cache.RepoInfo
import zielu.gittoolbox.util.AppUtil
import zielu.gittoolbox.util.DisposeSafeRunnable
import zielu.gittoolbox.util.GtUtil
import zielu.gittoolbox.util.ReschedulingExecutor
import java.util.concurrent.TimeUnit

internal class BehindTrackerSubscriber(private val project: Project) {
  private val executor: ReschedulingExecutor

  init {
    val scheduledExecutor = AppExecutorUtil.createBoundedScheduledExecutorService("GtBehindTracker", 1)
    executor = ReschedulingExecutor(scheduledExecutor, true)
    Disposer.register(project, executor)
  }

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

  companion object {
    private val log = Logger.getInstance(BehindTrackerSubscriber::class.java)

    fun getInstance(project: Project): BehindTrackerSubscriber {
      return AppUtil.getServiceInstance(project, BehindTrackerSubscriber::class.java)
    }
  }
}
