package zielu.gittoolbox.fetch

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.util.concurrency.AppExecutorUtil
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
  private val autoFetchExecutor = AppExecutorUtil.createBoundedScheduledExecutorService("GtAutoFetch", 1)
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
      Optional.of(schedule(delay, task))
    } else {
      Optional.empty()
    }
  }

  private fun schedule(delay: Duration, task: Runnable): ScheduledFuture<*> {
    return autoFetchExecutor.schedule(task, delay.toMillis(), TimeUnit.MILLISECONDS)
  }

  override fun dispose() {
    if (active.compareAndSet(true, false)) {
      autoFetchExecutor.shutdownNow()
    }
  }

  companion object {
    private val log = Logger.getInstance(AutoFetchGateway::class.java)

    @JvmStatic
    fun getInstance(project: Project): AutoFetchGateway {
      return getServiceInstance(project, AutoFetchGateway::class.java)
    }
  }
}
