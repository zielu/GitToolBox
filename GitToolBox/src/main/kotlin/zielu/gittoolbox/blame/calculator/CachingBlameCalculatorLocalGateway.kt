package zielu.gittoolbox.blame.calculator

import com.google.common.cache.Cache
import com.intellij.openapi.project.Project
import zielu.gittoolbox.metrics.CacheMetrics
import zielu.gittoolbox.metrics.ProjectMetrics

internal class CachingBlameCalculatorLocalGateway(private val project: Project) {
  private val incrementalCalculator = IncrementalBlameCalculator(project)

  fun calculator(): BlameCalculator = incrementalCalculator

  fun exposeCacheMetrics(cache: Cache<*, *>) {
    CacheMetrics.expose(cache, ProjectMetrics.getInstance(project), "blame-calculator-cache")
  }
}
