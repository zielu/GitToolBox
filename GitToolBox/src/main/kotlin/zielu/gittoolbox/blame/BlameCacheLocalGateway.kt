package zielu.gittoolbox.blame

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.history.VcsRevisionNumber
import com.intellij.openapi.vfs.VirtualFile
import git4idea.repo.GitRepository
import zielu.gittoolbox.cache.VirtualFileRepoCache
import zielu.gittoolbox.util.ExecutableTask
import zielu.gittoolbox.util.LocalGateway
import zielu.intellij.metrics.GtTimer

internal class BlameCacheLocalGateway(
  private val project: Project
) : Disposable, LocalGateway(
  project
) {
  fun getCacheGetTimer(): GtTimer = getMetrics().timer("blame-cache.get")

  fun getLoadTimer(): GtTimer = getMetrics().timer("blame-cache.load")

  fun getQueueWaitTimer(): GtTimer = getMetrics().timer("blame-cache.queue-wait")

  fun fireBlameUpdated(vFile: VirtualFile, annotation: BlameAnnotation) {
    publishAsync(this) { it.syncPublisher(BlameCache.CACHE_UPDATES).cacheUpdated(vFile, annotation) }
  }

  fun fireBlameInvalidated(vFile: VirtualFile) {
    publishAsync(this) { it.syncPublisher(BlameCache.CACHE_UPDATES).invalidated(vFile) }
  }

  fun getRepoForFile(vFile: VirtualFile): GitRepository? {
    return VirtualFileRepoCache.getInstance(project).getRepoForFile(vFile)
  }

  fun getBlameLoader(): BlameLoader {
    return BlameLoader.getInstance(project)
  }

  fun execute(task: ExecutableTask) {
    BlameCacheExecutor.getInstance(project).ifPresent { it.execute(task) }
  }

  fun getCurrentRevision(repository: GitRepository): VcsRevisionNumber {
    return getBlameLoader().getCurrentRevision(repository)
  }

  fun registerQueuedGauge(gauge: () -> Int) {
    getMetrics().gauge("blame-cache.queue.size", gauge)
  }

  fun submitDiscarded() {
    getMetrics().counter("blame-cache.discarded.count").inc()
  }

  fun invalidateForRoot(root: VirtualFile) {
    BlameLoader.getExistingInstance(project).ifPresent { it.invalidateForRoot(root) }
  }

  override fun dispose() {
    // TODO: implement
  }
}
