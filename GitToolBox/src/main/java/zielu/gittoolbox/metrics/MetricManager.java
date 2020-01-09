package zielu.gittoolbox.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.Timer;
import org.jetbrains.annotations.NotNull;

class MetricManager implements Metrics {
  private final MetricRegistry registry = new MetricRegistry();

  MetricRegistry getRegistry() {
    return registry;
  }

  private String name(@NotNull String simpleName) {
    return MetricRegistry.name(simpleName);
  }

  @Override
  public Timer timer(@NotNull String simpleName) {
    return registry.timer(name(simpleName));
  }

  @Override
  public Counter counter(@NotNull String simpleName) {
    return registry.counter(name(simpleName));
  }

  @Override
  public <T> Gauge gauge(@NotNull String simpleName, @NotNull Gauge<T> gauge) {
    String name = name(simpleName);
    return registry.gauge(name, () -> gauge);
  }

  @Override
  public void addAll(@NotNull MetricSet metricSet) {
    registry.registerAll(metricSet);
  }
}
