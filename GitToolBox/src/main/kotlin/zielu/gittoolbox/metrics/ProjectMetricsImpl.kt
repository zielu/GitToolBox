package zielu.gittoolbox.metrics

import com.codahale.metrics.Counter
import com.codahale.metrics.Gauge
import com.codahale.metrics.MetricSet
import com.codahale.metrics.Timer
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import zielu.gittoolbox.lifecycle.ProjectLifecycleNotifier
import zielu.gittoolbox.metrics.Jmx.startReporting
import zielu.gittoolbox.util.DisposeSafeRunnable

internal class ProjectMetricsImpl(project: Project) : ProjectMetrics {
  private val metrics = MetricsManager()

  init {
    // TODO: convert to declarative
    project.messageBus.connect(project).subscribe(ProjectLifecycleNotifier.TOPIC, object : ProjectLifecycleNotifier {
      override fun projectReady(project: Project) {
        ApplicationManager.getApplication()
          .executeOnPooledThread(DisposeSafeRunnable(project, Runnable { startReporter(project) }))
      }
    })
  }

  private fun startReporter(project: Project) {
    val reporter = startReporting(project, metrics.getRegistry())
    // TODO: local gateway
    Disposer.register(project, reporter)
  }

  override fun addAll(metricSet: MetricSet) = metrics.addAll(metricSet)

  override fun timer(simpleName: String): Timer = metrics.timer(simpleName)

  override fun counter(simpleName: String): Counter = metrics.counter(simpleName)

  override fun <T : Any?> gauge(simpleName: String, value: () -> T): Gauge<*> {
    return metrics.gauge(simpleName, value)
  }
}
