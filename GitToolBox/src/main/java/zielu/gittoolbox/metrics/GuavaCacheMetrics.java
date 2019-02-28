package zielu.gittoolbox.metrics;

import static com.codahale.metrics.MetricRegistry.name;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheStats;
import java.util.Map;

public class GuavaCacheMetrics implements MetricSet {
  private final MetricManager manager = new MetricManager();

  public GuavaCacheMetrics(Cache<?, ?> cache, String cacheName) {
    manager.gauge(name(cacheName, "size"), cache::size);
    CacheStats stats = cache.stats();
    manager.gauge(name(cacheName, "hit", "rate"), stats::hitRate);
    manager.gauge(name(cacheName, "miss", "rate"), stats::missRate);
    manager.gauge(name(cacheName, "load", "count"), stats::loadCount);
    manager.gauge(name(cacheName, "avg", "load", "time"), stats::averageLoadPenalty);
    manager.gauge(name(cacheName, "eviction", "count"), stats::evictionCount);
  }

  @Override
  public Map<String, Metric> getMetrics() {
    return manager.getRegistry().getMetrics();
  }
}
