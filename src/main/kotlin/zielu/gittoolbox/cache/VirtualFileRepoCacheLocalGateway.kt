package zielu.gittoolbox.cache

import com.google.common.cache.Cache
import java.util.function.Supplier

internal interface VirtualFileRepoCacheLocalGateway {
  fun fireCacheChanged()

  fun rootsVFileCacheSizeGauge(size: () -> Int)

  fun rootsFilePathCacheSizeGauge(size: () -> Int)

  fun exposeDirsCacheMetrics(cache: Cache<*, *>)

  fun exposeFilePathsCacheMetrics(cache: Cache<*, *>)

  fun <T> repoForDirCacheTimer(supplier: Supplier<T>): T
}
