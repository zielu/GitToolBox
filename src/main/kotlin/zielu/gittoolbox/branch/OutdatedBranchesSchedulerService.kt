package zielu.gittoolbox.branch

import com.intellij.notification.Notification
import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import git4idea.repo.GitRepository
import git4idea.util.GitUIUtil
import jodd.util.StringBand
import zielu.gittoolbox.GitToolBoxApp
import zielu.gittoolbox.ResBundle
import zielu.gittoolbox.concurrent.executeAsync
import zielu.gittoolbox.config.ProjectConfig
import zielu.gittoolbox.notification.GtNotifier
import zielu.gittoolbox.ui.branch.OutdatedBranchesDialog
import zielu.gittoolbox.ui.util.AppUiUtil
import zielu.gittoolbox.util.AppUtil
import zielu.gittoolbox.util.Html
import zielu.intellij.concurrent.ZDisposableRunnableWrapper
import zielu.intellij.util.ZDisposeGuard
import java.time.Duration
import java.util.concurrent.TimeUnit

internal class OutdatedBranchesSchedulerService(
  private val project: Project
) : Disposable {
  private val disposeGuard = ZDisposeGuard()

  private var notScheduled = true
  private var previousNotification: Notification? = null

  init {
    Disposer.register(this, disposeGuard)
  }

  fun setupSchedule() {
    disposeGuard.ifActive {
      if (notScheduled) {
        schedule(true)
        notScheduled = false
      }
    }
  }

  private fun schedule(firstTime: Boolean) {
    // TODO: reschedule or cancel on config change

    val config = ProjectConfig.getMerged(project)
    if (config.outdatedBranchesAutoCleanupEnabled()) {
      GitToolBoxApp.getInstance().ifPresent { app ->
        val delay = if (firstTime)
          Duration.ofMinutes(30)
        else
          Duration.ofHours(config.outdatedBranchesAutoCleanupIntervalHours().toLong())
        val task = ZDisposableRunnableWrapper(Task(project, { handle(it) }, { schedule(false) }))
        Disposer.register(disposeGuard, task)
        app.schedule(task, delay.toMinutes(), TimeUnit.MINUTES)
      }
    }
  }

  private fun handle(outdatedBranches: Map<GitRepository, List<OutdatedBranch>>) {
    previousNotification?.apply { expire() }
    previousNotification = null

    if (outdatedBranches.isNotEmpty()) {
      val count = outdatedBranches.values.sumOf { it.size }
      val message = StringBand("Found ")
        .append(count).append(" outdated branches")
        .append(Html.BRX)
        .append(Html.link("details", "details..."))
      previousNotification = GtNotifier.getInstance(project).branchCleanupSuccess(
        GitUIUtil.bold(ResBundle.message("branch.cleanup.notification.success.title")),
        message.toString()
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
    // do nothing
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
