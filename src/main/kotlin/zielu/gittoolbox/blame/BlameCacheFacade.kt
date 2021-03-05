package zielu.gittoolbox.blame

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.history.VcsRevisionNumber
import com.intellij.openapi.vfs.VirtualFile
import git4idea.repo.GitRepository
import zielu.gittoolbox.cache.VirtualFileRepoCache
import zielu.gittoolbox.util.ExecutableTask
import zielu.gittoolbox.util.PrjBaseFacade
import zielu.intellij.util.ZDisposeGuard
import java.util.concurrent.TimeUnit

internal class BlameCacheFacade(
  private val project: Project
) : Disposable, PrjBaseFacade(
  project
) {
  private val disposeGuard = ZDisposeGuard()

  fun timeCacheGet(get: () -> BlameAnnotation): BlameAnnotation {
    return if (disposeGuard.isActive()) {
      getMetrics().timer("blame-cache.get").timeSupplierKt(get)
    } else {
      BlameAnnotation.EMPTY
    }
  }

  fun timeLoad(load: () -> BlameAnnotation): BlameAnnotation {
    return if (disposeGuard.isActive()) {
      getMetrics().timer("blame-cache.load").timeSupplierKt(load)
    } else {
      BlameAnnotation.EMPTY
    }
  }

  fun updateQueueWait(duration: Long, unit: TimeUnit) {
    if (disposeGuard.isActive()) {
      getMetrics().timer("blame-cache.queue-wait").update(duration, unit)
    }
  }

  fun fireBlameUpdated(vFile: VirtualFile, annotation: BlameAnnotation) {
    publishAsync(this) { it.syncPublisher(BlameCache.CACHE_UPDATES).cacheUpdated(vFile, annotation) }
  }

  fun fireBlameInvalidated(vFile: VirtualFile) {
    publishAsync(this) { it.syncPublisher(BlameCache.CACHE_UPDATES).invalidated(vFile) }
  }

  fun getRepoForFile(vFile: VirtualFile): GitRepository? {
    return if (disposeGuard.isActive()) {
      VirtualFileRepoCache.getInstance(project).getRepoForFile(vFile)
    } else {
      null
    }
  }

  fun getBlameLoader(): BlameLoader {
    return if (disposeGuard.isActive()) {
      BlameLoader.getInstance(project)
    } else {
      NullBlameLoader
    }
  }

  fun execute(task: ExecutableTask) {
    if (disposeGuard.isActive()) {
      BlameCacheExecutor.getInstance(project).ifPresent { it.execute(task) }
    }
  }

  fun getCurrentRevision(repository: GitRepository): VcsRevisionNumber {
    return getBlameLoader().getCurrentRevision(repository)
  }

  fun registerQueuedGauge(gauge: () -> Int) {
    if (disposeGuard.isActive()) {
      getMetrics().gauge("blame-cache.queue.size", gauge)
    }
  }

  fun submitDiscarded() {
    if (disposeGuard.isActive()) {
      getMetrics().counter("blame-cache.discarded.count").inc()
    }
  }

  fun invalidateForRoot(root: VirtualFile) {
    if (disposeGuard.isActive()) {
      BlameLoader.getExistingInstance(project).ifPresent { it.invalidateForRoot(root) }
    }
  }

  override fun dispose() {
    dispose(disposeGuard)
  }
}
