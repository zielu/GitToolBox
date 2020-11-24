package zielu.intellij.concurrent

import zielu.intellij.metrics.Metrics
import java.util.concurrent.ThreadFactory

internal class ZThreadFactory(threadGroupName: String) : ThreadFactory {
  private val group = ThreadGroup(threadGroupName)
  private var created: Int = 0

  override fun newThread(task: Runnable): Thread {
    created++
    return Thread(group, task)
  }

  fun exposeMetrics(metrics: Metrics) {
    metrics.gauge("${group.name}.active") { group.activeCount() }
    metrics.gauge("${group.name}.created") { created }
  }
}
