package zielu.gittoolbox.fetch

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.util.concurrency.AppExecutorUtil
import zielu.gittoolbox.util.AppUtil.getServiceInstance
import zielu.gittoolbox.util.GatewayBase
import java.time.Clock
import java.time.Duration
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.function.BiFunction

internal class AutoFetchGateway(private val prj: Project) : GatewayBase(prj) {
  private val clock: Clock by lazy {
    Clock.systemDefaultZone()
  }
  private val autoFetchExecutor by lazy {
    AppExecutorUtil.createBoundedScheduledExecutorService("GtAutoFetch", 1)
  }

  fun getNowMillis(): Long = clock.millis()

  fun scheduleAutoFetch(
    delay: Duration,
    taskCreator: BiFunction<Project, AutoFetchSchedule, Runnable>
  ): ScheduledFuture<*> {
    val task = taskCreator.apply(prj, AutoFetchSchedule.getInstance(prj))
    log.debug("Scheduling auto-fetch in ", delay)
    return schedule(delay, task)
  }

  private fun schedule(delay: Duration, task: Runnable): ScheduledFuture<*> {
    return autoFetchExecutor.schedule(task, delay.toMillis(), TimeUnit.MILLISECONDS)
  }

  companion object {
    private val log = Logger.getInstance(AutoFetchGateway::class.java)

    @JvmStatic
    fun getInstance(project: Project): AutoFetchGateway {
      return getServiceInstance(project, AutoFetchGateway::class.java)
    }
  }
}
