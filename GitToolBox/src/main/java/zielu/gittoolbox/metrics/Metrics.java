package zielu.gittoolbox.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Timer;
import org.jetbrains.annotations.NotNull;

public interface Metrics {
  Timer timer(@NotNull String simpleName);
  Counter counter(@NotNull String simpleName);
  <T> Gauge<T> gauge(@NotNull String simpleName, Gauge<T> gauge);
}
