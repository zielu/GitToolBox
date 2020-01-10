package zielu.gittoolbox.metrics

import com.codahale.metrics.Counter
import com.codahale.metrics.Gauge
import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.MetricSet
import com.codahale.metrics.Timer

internal class MetricsManager : Metrics {
  private val registry = MetricRegistry()

  override fun timer(simpleName: String): Timer {
    return registry.timer(name(simpleName))
  }

  override fun counter(simpleName: String): Counter {
    return registry.counter(name(simpleName))
  }

  override fun <T> gauge(simpleName: String, value: () -> T): Gauge<*> {
    return registry.gauge(name(simpleName)) { Gauge { value } }
  }

  override fun addAll(metricSet: MetricSet) {
    registry.registerAll(metricSet)
  }

  private fun name(originalName: String): String = MetricRegistry.name(originalName)

  fun getRegistry(): MetricRegistry = registry
}
