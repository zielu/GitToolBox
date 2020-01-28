package zielu.gittoolbox.metrics

import com.codahale.metrics.Counter
import com.codahale.metrics.Gauge
import com.codahale.metrics.MetricSet
import com.codahale.metrics.Timer

internal interface Metrics {
  fun timer(simpleName: String): Timer

  fun counter(simpleName: String): Counter

  fun <T> gauge(simpleName: String, value: () -> T): Gauge<*>

  fun addAll(metricSet: MetricSet)
}
