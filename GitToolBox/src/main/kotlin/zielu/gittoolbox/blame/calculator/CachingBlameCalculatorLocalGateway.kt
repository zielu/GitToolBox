package zielu.gittoolbox.blame.calculator

import com.google.common.cache.Cache
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vcs.history.VcsRevisionNumber
import com.intellij.openapi.vfs.VirtualFile
import zielu.gittoolbox.blame.calculator.persistence.BlameCalculationPersistence
import zielu.gittoolbox.metrics.CacheMetrics
import zielu.gittoolbox.metrics.ProjectMetrics
import zielu.gittoolbox.revision.RevisionDataProvider

internal class CachingBlameCalculatorLocalGateway(private val project: Project) {
  private val incrementalCalculator = IncrementalBlameCalculator(project)

  fun calculator(): BlameCalculator = incrementalCalculator

  fun exposeCacheMetrics(cache: Cache<*, *>) {
    CacheMetrics.expose(cache, ProjectMetrics.getInstance(project), "blame-calculator-cache")
  }

  fun shouldLoadFromPersistence(): Boolean {
    return Registry.`is`("zielu.gittoolbox.blame.cache.persistent", true)
  }

  fun loadFromPersistence(file: VirtualFile, revision: VcsRevisionNumber): RevisionDataProvider? {
    return ProjectMetrics.getInstance(project)
      .timer("blame-calculator-cache.load-persisted").timeSupplier {
        BlameCalculationPersistence.getInstance(project).getBlame(file, revision)
      }
  }

  fun storeInPersistence(dataProvider: RevisionDataProvider) {
    BlameCalculationPersistence.getInstance(project).storeBlame(dataProvider)
  }
}
