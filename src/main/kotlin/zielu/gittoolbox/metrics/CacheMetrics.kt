package zielu.gittoolbox.metrics

import com.google.common.cache.Cache
import zielu.intellij.metrics.Metrics

internal object CacheMetrics {
  fun expose(cache: Cache<*, *>, metrics: Metrics, cacheName: String) {
    metrics.gauge("$cacheName.size") { cache.size() }
    metrics.gauge("$cacheName.hitRate") { cache.stats().hitRate() }
    metrics.gauge("$cacheName.loadCount") { cache.stats().loadCount() }
    metrics.gauge("$cacheName.loadExceptionCount") { cache.stats().loadExceptionCount() }
  }
}
