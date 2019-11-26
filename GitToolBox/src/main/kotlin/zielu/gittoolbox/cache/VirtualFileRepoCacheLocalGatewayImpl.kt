package zielu.gittoolbox.cache

import com.intellij.openapi.project.Project
import zielu.gittoolbox.cache.VirtualFileRepoCache.CACHE_CHANGE
import zielu.gittoolbox.util.LocalGateway
import java.util.function.Supplier

internal class VirtualFileRepoCacheLocalGatewayImpl(
  project: Project
) : LocalGateway(project), VirtualFileRepoCacheLocalGateway {
  private val messageBus by lazy {
    project.messageBus
  }

  override fun fireCacheChanged() {
    messageBus.syncPublisher<VirtualFileCacheListener>(CACHE_CHANGE).updated()
  }

  override fun rootsCacheSizeGauge(size: () -> Int) {
    getMetrics().gauge("vfile-repo-roots-cache-size", size)
  }

  override fun dirsCacheSizeGauge(size: () -> Int) {
    getMetrics().gauge("vfile-repo-dirs-cache-size", size)
  }

  override fun <T> repoForDirCacheTimer(supplier: Supplier<T>): T {
    return getMetrics().timer("repo-for-dir-cache").timeSupplier(supplier)
  }
}
