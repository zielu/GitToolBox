package zielu.gittoolbox.branch

import com.intellij.notification.Notification
import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import git4idea.repo.GitRepository
import git4idea.util.GitUIUtil
import zielu.gittoolbox.GitToolBoxApp
import zielu.gittoolbox.ResBundle
import zielu.gittoolbox.concurrent.executeAsync
import zielu.gittoolbox.config.MergedProjectConfig
import zielu.gittoolbox.config.ProjectConfig
import zielu.gittoolbox.notification.GtNotifier
import zielu.gittoolbox.ui.branch.OutdatedBranchesDialog
import zielu.gittoolbox.ui.util.AppUiUtil
import zielu.gittoolbox.util.AppUtil
import zielu.intellij.concurrent.ZDisposableRunnableWrapper
import zielu.intellij.util.ZDisposeGuard
import java.time.Duration
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

internal class OutdatedBranchesSchedulerService(
  private val project: Project
) : Disposable {
  private val disposeGuard = ZDisposeGuard()

  private var notScheduled = AtomicBoolean(true)
  private var previousNotification: Notification? = null
  private var scheduledTask: ScheduledFuture<*>? = null

  init {
    Disposer.register(this, disposeGuard)
  }

  fun setupSchedule() {
    disposeGuard.ifActive {
      schedule()
    }
  }

  private fun schedule() {
    if (notScheduled.compareAndSet(true, false)) {
      schedule(true)
    }
  }

  @Synchronized
  fun onConfigChanged(previous: MergedProjectConfig, current: MergedProjectConfig) {
    val currentEnabled = current.outdatedBranchesAutoCleanupEnabled()
    if (previous.outdatedBranchesAutoCleanupEnabled() != current.outdatedBranchesAutoCleanupEnabled()) {
      // start or stop schedule
      if (currentEnabled) {
        schedule()
      } else {
        cancelCurrentTask()
        notScheduled.compareAndSet(false, true)
      }
    } else {
      if (currentEnabled) {
        rescheduleCurrentTask(current.outdatedBranchesAutoCleanupIntervalHours())
      }
    }
  }

  @Synchronized
  private fun cancelCurrentTask() {
    scheduledTask?.apply { cancel(true) }
  }

  @Synchronized
  private fun rescheduleCurrentTask(currentIntervalHours: Int) {
    val intervalMinutes = Duration.ofHours(currentIntervalHours.toLong()).toMinutes()
    val remainingDelay = scheduledTask?.getDelay(TimeUnit.MINUTES)
    if (remainingDelay != null) {
      if (remainingDelay > intervalMinutes) {
        cancelCurrentTask()
        scheduleTask(Duration.ofMinutes(intervalMinutes))
      }
    } else {
      log.error("Should reschedule absent branch cleanup task")
    }
  }

  private fun schedule(firstTime: Boolean) {
    val config = ProjectConfig.getMerged(project)
    if (config.outdatedBranchesAutoCleanupEnabled()) {
      val delay = if (firstTime)
        Duration.ofMinutes(30)
      else
        Duration.ofHours(config.outdatedBranchesAutoCleanupIntervalHours().toLong())
      scheduleTask(delay)
    }
  }

  @Synchronized
  private fun scheduleTask(delay: Duration) {
    GitToolBoxApp.getInstance().ifPresent { app ->
      val task = ZDisposableRunnableWrapper(Task(project, { handle(it) }, { schedule(false) }))
      Disposer.register(disposeGuard, task)
      scheduledTask = app.schedule(task, delay.toMinutes(), TimeUnit.MINUTES)
    }
  }

  private fun handle(outdatedBranches: Map<GitRepository, List<OutdatedBranch>>) {
    previousNotification?.apply { expire() }
    previousNotification = null

    if (outdatedBranches.isNotEmpty()) {
      val count = outdatedBranches.values.sumOf { it.size }
      val message = ResBundle.message("branch.cleanup.cleaner.schedule.message", count)
      previousNotification = GtNotifier.getInstance(project).branchCleanupSuccess(
        GitUIUtil.bold(ResBundle.message("branch.cleanup.notification.success.title")),
        message
      ) { _, _ ->
        cleanupBranches(outdatedBranches)
      }
    } else {
      GtNotifier.getInstance(project).branchCleanupSuccess(
        GitUIUtil.bold(ResBundle.message("branch.cleanup.notification.success.title")),
        ResBundle.message("branch.cleanup.notification.nothing.found")
      )
    }
  }

  private fun cleanupBranches(outdatedBranches: Map<GitRepository, List<OutdatedBranch>>) {
    AppUiUtil.invokeLaterIfNeeded {
      val dialog = OutdatedBranchesDialog(project)
      dialog.setData(outdatedBranches)
      val performCleanup = dialog.showAndGet()
      if (performCleanup) {
        BranchCleaner(project, dialog.getData()).queue()
      }
    }
  }

  override fun dispose() {
    cancelCurrentTask()
  }

  companion object {
    fun getInstance(project: Project): OutdatedBranchesSchedulerService {
      return AppUtil.getServiceInstance(project, OutdatedBranchesSchedulerService::class.java)
    }
  }
}

private val log: Logger = Logger.getInstance(OutdatedBranchesSchedulerService::class.java)

private class Task(
  private val project: Project,
  private val onDone: (Map<GitRepository, List<OutdatedBranch>>) -> Unit,
  private val onComplete: () -> Unit
) : Runnable {
  override fun run() {
    OutdatedBranchesCollector(project).executeAsync().whenComplete { outdatedBranches, error ->
      if (error != null) {
        log.error("Outdated branches calculation failed", error)
      } else {
        onDone.invoke(outdatedBranches)
      }
      onComplete.invoke()
    }
  }
}
