package zielu.gittoolbox.status.behindtracker

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import git4idea.repo.GitRepository
import zielu.gittoolbox.GitToolBoxApp
import zielu.gittoolbox.cache.RepoInfo
import zielu.gittoolbox.util.AppUtil
import zielu.gittoolbox.util.GtUtil
import zielu.gittoolbox.util.ReschedulingExecutor
import zielu.intellij.concurrent.DisposeSafeRunnable
import zielu.intellij.util.ZDisposeGuard
import java.util.concurrent.TimeUnit

internal class BehindTrackerSubscriber(
  private val project: Project
) : Disposable {
  private val disposeGuard = ZDisposeGuard()
  private val executor: ReschedulingExecutor = GitToolBoxApp.createReschedulingExecutor()
  init {
    Disposer.register(this, executor)
    Disposer.register(this, disposeGuard)
  }

  fun onStateChanged(repoInfo: RepoInfo, repository: GitRepository) {
    if (log.isDebugEnabled) {
      log.debug("State changed [", GtUtil.name(repository), "]: ", repoInfo)
    }
    if (disposeGuard.isActive()) {
      BehindTracker.getInstance(project).onStateChange(repository, repoInfo)
      scheduleNotifyTask()
    }
  }

  private fun scheduleNotifyTask() {
    executor.schedule(
      "behind-notify",
      DisposeSafeRunnable(BehindNotifyTask(project, disposeGuard)),
      10,
      TimeUnit.SECONDS
    )
  }

  override fun dispose() {
    // do nothing
  }

  companion object {
    private val log = Logger.getInstance(BehindTrackerSubscriber::class.java)

    fun getInstance(project: Project): BehindTrackerSubscriber {
      return AppUtil.getServiceInstance(project, BehindTrackerSubscriber::class.java)
    }
  }
}
