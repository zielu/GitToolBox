package zielu.gittoolbox.metrics

internal object ThreadGroupMetrics {
  fun expose(group: ThreadGroup, metrics: Metrics) {
    metrics.gauge("${group.name}.active") { group.activeCount() }
  }
}
