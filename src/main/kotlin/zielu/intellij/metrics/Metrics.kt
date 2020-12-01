package zielu.intellij.metrics

internal interface Metrics {
  fun timer(simpleName: String): GtTimer

  fun counter(simpleName: String): GtCounter

  fun <T : Any?> gauge(simpleName: String, value: () -> T): GtGauge
}
