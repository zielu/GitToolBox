package zielu.gittoolbox.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Timer;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

class GitToolBoxProjectMetrics implements ProjectComponent, Metrics {
  private final MetricManager metrics = new MetricManager();
  private final Project project;

  GitToolBoxProjectMetrics(@NotNull Project project) {
    this.project = project;
  }

  static Metrics getInstance(@NotNull Project project) {
    return project.getComponent(GitToolBoxProjectMetrics.class);
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
    return gauge(simpleName, gauge);
  }
}
