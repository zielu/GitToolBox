package zielu.gittoolbox.fetch

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import zielu.gittoolbox.GitToolBoxApp
import zielu.gittoolbox.util.AppUtil.getServiceInstance
import zielu.intellij.concurrent.DisposeSafeRunnable
import zielu.intellij.concurrent.ZDisposableRunnable
import zielu.intellij.util.ZDisposeGuard
import java.time.Clock
import java.time.Duration
import java.util.Optional
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.function.BiFunction

internal class AutoFetchFacade(
  private val prj: Project
) : Disposable {
  private val disposeGuard = ZDisposeGuard()
  private val clock: Clock by lazy {
    Clock.systemDefaultZone()
  }

  fun getNowMillis(): Long = clock.millis()

  fun scheduleAutoFetch(
    delay: Duration,
    taskCreator: BiFunction<Project, AutoFetchSchedule, ZDisposableRunnable>
  ): Optional<ScheduledFuture<*>> {
    return if (disposeGuard.isActive()) {
      val task = taskCreator.apply(prj, AutoFetchSchedule.getInstance(prj))
      log.debug("Scheduling auto-fetch in ", delay)
      Optional.ofNullable(schedule(delay, task))
    } else {
      Optional.empty()
    }
  }

  private fun schedule(delay: Duration, task: ZDisposableRunnable): ScheduledFuture<*>? {
    Disposer.register(this, task)
    return GitToolBoxApp.getInstance()
      .map { app -> app.schedule(DisposeSafeRunnable(task), delay.toMillis(), TimeUnit.MILLISECONDS) }
      .orElse(null)
  }

  override fun dispose() {
    Disposer.dispose(disposeGuard)
  }

  companion object {
    private val log = Logger.getInstance(AutoFetchFacade::class.java)

    @JvmStatic
    fun getInstance(project: Project): AutoFetchFacade {
      return getServiceInstance(project, AutoFetchFacade::class.java)
    }
  }
}
