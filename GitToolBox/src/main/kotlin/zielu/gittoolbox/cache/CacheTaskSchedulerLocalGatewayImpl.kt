package zielu.gittoolbox.cache

import com.codahale.metrics.Counter
import com.intellij.openapi.project.Project
import zielu.gittoolbox.util.LocalGateway

internal class CacheTaskSchedulerLocalGatewayImpl(
  project: Project
) : LocalGateway(project), CacheTaskSchedulerLocalGateway {
  override fun queueSizeCounterInc() {
    getQueueSizeCounter().inc()
  }

  override fun queueSizeCounterDec() {
    getQueueSizeCounter().dec()
  }

  private fun getQueueSizeCounter(): Counter {
    return getMetrics().counter("info-cache-queue-size")
  }

  override fun discardedTasksCounterInc() {
    return getMetrics().counter("info-cache-discarded-updates").inc()
  }
}
