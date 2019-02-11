package zielu.gittoolbox.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Timer;
import org.jetbrains.annotations.NotNull;

public class MockMetrics implements AppMetrics, ProjectMetrics {
  private Metrics metrics = new MetricManager();

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
