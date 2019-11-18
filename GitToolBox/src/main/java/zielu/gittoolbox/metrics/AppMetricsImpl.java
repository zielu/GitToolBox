package zielu.gittoolbox.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.Timer;
import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;

class AppMetricsImpl implements AppMetrics {
  private final MetricManager metrics = new MetricManager();

  AppMetricsImpl() {
    ApplicationManager.getApplication().executeOnPooledThread(this::startReporter);
  }

  private void startReporter() {
    Jmx.reporter(metrics.getRegistry());
  }

  private String name(@NotNull String simpleName) {
    return MetricRegistry.name(simpleName);
  }

  @Override
  public Timer timer(@NotNull String simpleName) {
    return metrics.timer(name(simpleName));
  }

  @Override
  public Counter counter(@NotNull String simpleName) {
    return metrics.counter(name(simpleName));
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
