package zielu.gittoolbox.cache

import com.intellij.openapi.project.Project
import zielu.gittoolbox.GitToolBoxApp
import zielu.gittoolbox.util.LocalGateway
import zielu.intellij.metrics.GtCounter
import java.util.concurrent.TimeUnit

internal class CacheTaskSchedulerLocalGatewayImpl(
  project: Project
) : LocalGateway(project), CacheTaskSchedulerLocalGateway {
  override fun queueSizeCounterInc() {
    getQueueSizeCounter().inc()
  }

  override fun queueSizeCounterDec() {
    getQueueSizeCounter().dec()
  }

  private fun getQueueSizeCounter(): GtCounter {
    return getMetrics().counter("info-cache.queue.size")
  }

  override fun discardedTasksCounterInc() {
    return getMetrics().counter("info-cache.discarded-updates.count").inc()
  }

  override fun schedule(task: Runnable, taskDelayMillis: Long) {
    GitToolBoxApp.getInstance().ifPresent {
      it.schedule(task, taskDelayMillis, TimeUnit.MILLISECONDS)
    }
  }
}
