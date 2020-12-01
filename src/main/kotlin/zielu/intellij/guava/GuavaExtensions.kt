package zielu.intellij.guava

import com.google.common.cache.LoadingCache
import com.intellij.openapi.diagnostic.Logger
import java.util.concurrent.ExecutionException

internal fun <K, V> LoadingCache<K, V>.getSafe(key: K, errorValue: V): V {
  return try {
    this.get(key)
  } catch (e: ExecutionException) {
    Logger.getInstance(LoadingCache::class.java).warn("Failed to load: $key", e)
    errorValue
  }
}
