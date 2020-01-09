package zielu.gittoolbox.metrics

import zielu.gittoolbox.util.AppUtil

internal interface AppMetrics : Metrics {
  companion object {
    @JvmStatic
    fun getInstance(): Metrics {
      return AppUtil.getServiceInstance(AppMetrics::class.java)
    }
  }
}
