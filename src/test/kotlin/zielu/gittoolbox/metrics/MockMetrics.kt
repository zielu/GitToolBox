package zielu.gittoolbox.metrics

import zielu.intellij.metrics.GtCounter
import zielu.intellij.metrics.GtGauge
import zielu.intellij.metrics.GtTimer
import zielu.intellij.metrics.codehale.CodehaleMetricsManager

internal class MockMetrics : AppMetrics, ProjectMetrics {
  private val metrics = CodehaleMetricsManager()

  override fun startReporting() {
    // do nothing
  }

  override fun timer(simpleName: String): GtTimer = metrics.timer(simpleName)

  override fun counter(simpleName: String): GtCounter = metrics.counter(simpleName)

  override fun <T : Any?> gauge(simpleName: String, value: () -> T): GtGauge {
    return metrics.gauge(simpleName, value)
  }
}
