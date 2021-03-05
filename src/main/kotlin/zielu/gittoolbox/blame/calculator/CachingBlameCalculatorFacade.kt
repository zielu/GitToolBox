package zielu.gittoolbox.blame.calculator

import com.google.common.cache.Cache
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.history.VcsRevisionNumber
import com.intellij.openapi.vfs.VirtualFile
import zielu.gittoolbox.GitToolBoxRegistry
import zielu.gittoolbox.blame.calculator.persistence.BlameCalculationPersistence
import zielu.gittoolbox.metrics.CacheMetrics
import zielu.gittoolbox.metrics.ProjectMetrics
import zielu.gittoolbox.revision.RevisionDataProvider
import zielu.gittoolbox.util.PrjBaseFacade

internal class CachingBlameCalculatorFacade(private val project: Project) : PrjBaseFacade(project) {
  private val incrementalCalculator = IncrementalBlameCalculator(project)

  fun calculator(): BlameCalculator = incrementalCalculator

  fun exposeCacheMetrics(cache: Cache<*, *>) {
    CacheMetrics.expose(cache, ProjectMetrics.getInstance(project), "blame-calculator-cache")
  }

  fun shouldLoadFromPersistence(): Boolean {
    return GitToolBoxRegistry.shouldLoadBlameFromPersistence()
  }

  fun loadFromPersistence(file: VirtualFile, revision: VcsRevisionNumber): RevisionDataProvider? {
    return ProjectMetrics.getInstance(project)
      .timer("blame-calculator-cache.load-persisted").timeSupplierKt {
        BlameCalculationPersistence.getInstance(project).getBlame(file, revision)
      }
  }

  fun storeInPersistence(dataProvider: RevisionDataProvider) {
    BlameCalculationPersistence.getInstance(project).storeBlame(dataProvider)
  }
}
