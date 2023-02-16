package zielu.gittoolbox.cache

import com.intellij.openapi.project.Project
import zielu.gittoolbox.GitToolBoxApp
import zielu.gittoolbox.util.PrjBaseFacade
import zielu.intellij.metrics.GtCounter
import java.util.concurrent.TimeUnit

internal open class CacheTaskSchedulerFacade(
  project: Project
) : PrjBaseFacade(project) {
  fun queueSizeCounterInc() {
    getQueueSizeCounter().inc()
  }

  fun queueSizeCounterDec() {
    getQueueSizeCounter().dec()
  }

  private fun getQueueSizeCounter(): GtCounter {
    return getMetrics().counter("info-cache.queue.size")
  }

  fun discardedTasksCounterInc() {
    return getMetrics().counter("info-cache.discarded-updates.count").inc()
  }

  fun schedule(task: Runnable, taskDelayMillis: Long) {
    GitToolBoxApp.getInstance().ifPresent {
      it.schedule(task, taskDelayMillis, TimeUnit.MILLISECONDS)
    }
  }
}
