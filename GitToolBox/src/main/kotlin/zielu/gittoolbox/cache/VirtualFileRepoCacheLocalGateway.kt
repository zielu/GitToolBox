package zielu.gittoolbox.cache

import com.intellij.openapi.Disposable
import java.util.function.Supplier

internal interface VirtualFileRepoCacheLocalGateway {
  fun fireCacheChanged()

  fun rootsCacheSizeGauge(size: () -> Int)

  fun dirsCacheSizeGauge(size: () -> Int)

  fun <T> repoForDirCacheTimer(supplier: Supplier<T>): T

  fun disposeWithProject(disposable: Disposable)
}
