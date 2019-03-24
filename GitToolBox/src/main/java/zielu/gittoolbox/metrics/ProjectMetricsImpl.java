package zielu.gittoolbox.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.Timer;
import com.codahale.metrics.jmx.JmxReporter;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;

class ProjectMetricsImpl implements ProjectMetrics, Disposable {
  private final MetricManager metrics = new MetricManager();
  private final JmxReporter reporter;

  ProjectMetricsImpl(@NotNull Project project) {
    reporter = Jmx.reporter(project, metrics.getRegistry());
    reporter.start();
    Disposer.register(project, this);
  }

  @Override
  public void dispose() {
    reporter.close();
  }

  @Override
  public Timer timer(@NotNull String simpleName) {
    return metrics.timer(simpleName);
  }

  @Override
  public Counter counter(@NotNull String simpleName) {
    return metrics.counter(simpleName);
  }

  @Override
  public <T> Gauge gauge(@NotNull String simpleName, Gauge<T> gauge) {
    return metrics.gauge(simpleName, gauge);
  }

  @Override
  public void addAll(MetricSet metricSet) {
    metrics.addAll(metricSet);
  }
}
