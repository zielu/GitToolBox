package zielu.gittoolbox.cache

import com.google.common.cache.Cache
import com.intellij.openapi.project.Project
import zielu.gittoolbox.cache.VirtualFileRepoCache.CACHE_CHANGE
import zielu.gittoolbox.util.BaseFacade
import java.util.function.Supplier

internal class VirtualFileRepoCacheFacade(
  project: Project
) : BaseFacade(project) {

  fun fireCacheChanged() {
    publishSync { it.syncPublisher(CACHE_CHANGE).updated() }
  }

  fun rootsVFileCacheSizeGauge(size: () -> Int) {
    getMetrics().gauge("vfile-repo-roots-cache.size", size)
  }

  fun rootsFilePathCacheSizeGauge(size: () -> Int) {
    getMetrics().gauge("filepath-repo-roots-cache.size", size)
  }

  fun exposeDirsCacheMetrics(cache: Cache<*, *>) {
    exposeCacheMetrics(cache, "vfile-repo-dirs-cache")
  }

  fun exposeFilePathsCacheMetrics(cache: Cache<*, *>) {
    exposeCacheMetrics(cache, "filepath-repo-cache")
  }

  fun <T> repoForDirCacheTimer(supplier: Supplier<T>): T {
    return getMetrics().timer("repo-for-dir-cache.load").timeSupplier(supplier)
  }
}
