package zielu.gittoolbox.fetch

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import zielu.gittoolbox.GitToolBoxApp
import zielu.gittoolbox.util.AppUtil.getServiceInstance
import zielu.gittoolbox.util.GatewayBase
import java.time.Clock
import java.time.Duration
import java.util.Optional
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.BiFunction

internal class AutoFetchGateway(
  private val prj: Project
) : GatewayBase(prj), Disposable {
  private val active = AtomicBoolean(true)
  private val clock: Clock by lazy {
    Clock.systemDefaultZone()
  }

  fun getNowMillis(): Long = clock.millis()

  fun scheduleAutoFetch(
    delay: Duration,
    taskCreator: BiFunction<Project, AutoFetchSchedule, Runnable>
  ): Optional<ScheduledFuture<*>> {
    return if (active.get()) {
      val task = taskCreator.apply(prj, AutoFetchSchedule.getInstance(prj))
      log.debug("Scheduling auto-fetch in ", delay)
      Optional.ofNullable(schedule(delay, task))
    } else {
      Optional.empty()
    }
  }

  private fun schedule(delay: Duration, task: Runnable): ScheduledFuture<*>? {
    return GitToolBoxApp.getInstance()
      .map { app -> app.schedule(task, delay.toMillis(), TimeUnit.MILLISECONDS) }
      .orElse(null)
  }

  override fun dispose() {
    active.compareAndSet(true, false)
  }

  companion object {
    private val log = Logger.getInstance(AutoFetchGateway::class.java)

    @JvmStatic
    fun getInstance(project: Project): AutoFetchGateway {
      return getServiceInstance(project, AutoFetchGateway::class.java)
    }
  }
}
