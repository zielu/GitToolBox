package zielu.gittoolbox.metrics

import com.codahale.metrics.Counter
import com.codahale.metrics.Gauge
import com.codahale.metrics.MetricSet
import com.codahale.metrics.Timer

internal class MockMetrics : AppMetrics, ProjectMetrics {
  private val metrics = MetricManager()

  override fun addAll(metricSet: MetricSet) = metrics.addAll(metricSet)

  override fun timer(simpleName: String): Timer = metrics.timer(simpleName)

  override fun counter(simpleName: String): Counter = metrics.counter(simpleName)

  override fun <T : Any?> gauge(simpleName: String, gauge: Gauge<T>): Gauge<*> {
    return metrics.gauge(simpleName, gauge)
  }
}
