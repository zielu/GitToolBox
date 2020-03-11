package zielu.gittoolbox.metrics

import com.google.common.cache.Cache

internal object CacheMetrics {
  fun expose(cache: Cache<*, *>, metrics: Metrics, cacheName: String) {
    metrics.gauge("$cacheName.size") { cache.size() }
    metrics.gauge("$cacheName.hitRate") { cache.stats().hitRate() }
  }
}
