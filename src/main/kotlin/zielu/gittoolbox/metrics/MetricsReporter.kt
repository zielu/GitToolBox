package zielu.gittoolbox.metrics

import com.intellij.openapi.Disposable

internal interface MetricsReporter : Disposable {
  companion object {
    @JvmField
    val EMPTY: MetricsReporter = object : MetricsReporter {
      override fun dispose() {
        // do nothing
      }
    }
  }
}
