package zielu.gittoolbox.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Timer;
import com.codahale.metrics.jmx.JmxReporter;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

class ProjectMetricsImpl implements ProjectComponent, ProjectMetrics {
  private final MetricManager metrics = new MetricManager();
  private final Project project;
  private JmxReporter reporter;

  ProjectMetricsImpl(@NotNull Project project) {
    this.project = project;
  }

  @Override
  public void initComponent() {
    reporter = Jmx.report(project, metrics.getRegistry());
  }

  @Override
  public void projectOpened() {
    reporter.start();
  }

  @Override
  public void projectClosed() {
    if (reporter != null) {
      reporter.close();
    }
  }

  @Override
  public void disposeComponent() {
    reporter = null;
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
  public <T> Gauge<T> gauge(@NotNull String simpleName, Gauge<T> gauge) {
    return metrics.gauge(simpleName, gauge);
  }
}
