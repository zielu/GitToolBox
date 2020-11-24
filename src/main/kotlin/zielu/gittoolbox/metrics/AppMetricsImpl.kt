package zielu.gittoolbox.metrics

import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer
import zielu.intellij.metrics.GtCounter
import zielu.intellij.metrics.GtGauge
import zielu.intellij.metrics.GtTimer
import zielu.intellij.metrics.codehale.CodehaleMetricsManager

internal class AppMetricsImpl : AppMetrics, Disposable {
  private val metrics = CodehaleMetricsManager()

  override fun timer(simpleName: String): GtTimer = metrics.timer(simpleName)

  override fun counter(simpleName: String): GtCounter = metrics.counter(simpleName)

  override fun <T : Any?> gauge(simpleName: String, value: () -> T): GtGauge {
    return metrics.gauge(simpleName, value)
  }

  override fun startReporting() {
    Disposer.register(this, Jmx.startReporting(metrics.getRegistry()))
  }

  override fun dispose() {
  }
}
