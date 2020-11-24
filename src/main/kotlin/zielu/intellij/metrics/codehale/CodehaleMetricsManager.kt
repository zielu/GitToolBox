package zielu.intellij.metrics.codehale

import com.codahale.metrics.Gauge
import com.codahale.metrics.MetricRegistry
import zielu.intellij.metrics.GtCounter
import zielu.intellij.metrics.GtGauge
import zielu.intellij.metrics.GtTimer
import zielu.intellij.metrics.Metrics
import java.util.concurrent.ConcurrentHashMap

internal class CodehaleMetricsManager : Metrics {
  private val timers = ConcurrentHashMap<String, GtTimer>()
  private val counters = ConcurrentHashMap<String, GtCounter>()
  private val gauges = ConcurrentHashMap<String, GtGauge>()
  private val registry = MetricRegistry()

  override fun timer(simpleName: String): GtTimer {
    return timers.computeIfAbsent(name(simpleName)) { CodehaleTimer(registry.timer(it)) }
  }

  override fun counter(simpleName: String): GtCounter {
    return counters.computeIfAbsent(name(simpleName)) { CodehaleCounter(registry.counter(it)) }
  }

  override fun <T : Any?> gauge(simpleName: String, value: () -> T): GtGauge {
    return gauges.computeIfAbsent(name(simpleName)) { CodehaleGauge(registry.gauge(it) { Gauge { value.invoke() } }) }
  }

  private fun name(originalName: String): String = MetricRegistry.name(originalName)

  fun getRegistry(): MetricRegistry = registry
}
