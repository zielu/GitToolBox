package zielu.gittoolbox.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.Timer;
import org.jetbrains.annotations.NotNull;

public interface Metrics {
  Timer timer(@NotNull String simpleName);

  Counter counter(@NotNull String simpleName);

  <T> Gauge gauge(@NotNull String simpleName, @NotNull Gauge<T> gauge);

  void addAll(@NotNull MetricSet metricSet);
}
