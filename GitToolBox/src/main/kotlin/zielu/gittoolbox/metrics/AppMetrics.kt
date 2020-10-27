package zielu.gittoolbox.metrics

import zielu.gittoolbox.util.AppUtil

internal interface AppMetrics : Metrics {
  fun startReporting()

  companion object {
    @JvmStatic
    fun getInstance(): Metrics {
      return AppUtil.getServiceInstance(AppMetrics::class.java)
    }

    fun startReporting() {
      AppUtil.getServiceInstanceSafe(AppMetrics::class.java).ifPresent { it.startReporting() }
    }
  }
}
