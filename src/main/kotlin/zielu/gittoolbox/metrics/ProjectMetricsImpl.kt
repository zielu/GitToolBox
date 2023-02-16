package zielu.gittoolbox.metrics

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import zielu.gittoolbox.GitToolBoxApp
import zielu.gittoolbox.metrics.Jmx.startReporting
import zielu.intellij.concurrent.DisposeSafeRunnable
import zielu.intellij.concurrent.ZDisposableRunnableWrapper
import zielu.intellij.metrics.GtCounter
import zielu.intellij.metrics.GtGauge
import zielu.intellij.metrics.GtTimer
import zielu.intellij.metrics.codehale.CodehaleMetricsManager

internal class ProjectMetricsImpl(
  private val project: Project
) : ProjectMetrics, Disposable {
  private val metrics = CodehaleMetricsManager()

  override fun startReporting() {
    val operation = ZDisposableRunnableWrapper { startReporter(project) }
    Disposer.register(this, operation)
    GitToolBoxApp.getInstance().ifPresent {
      it.runInBackground(DisposeSafeRunnable(operation))
    }
  }

  private fun startReporter(project: Project) {
    Disposer.register(this, startReporting(project, metrics.getRegistry()))
  }

  override fun timer(simpleName: String): GtTimer = metrics.timer(simpleName)

  override fun counter(simpleName: String): GtCounter = metrics.counter(simpleName)

  override fun <T : Any?> gauge(simpleName: String, value: () -> T): GtGauge {
    return metrics.gauge(simpleName, value)
  }

  override fun dispose() {
    // do nothing
  }
}
