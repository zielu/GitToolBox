package zielu.intellij.guava

import com.google.common.cache.CacheLoader

internal abstract class ThreadLocalCacheLoader<C, K, V> : CacheLoader<K, V>() {
  private val contextStore = ThreadLocal<C>()

  fun setContext(context: C) {
    contextStore.set(context)
  }

  fun clearContext() {
    contextStore.remove()
  }

  override fun load(key: K): V {
    try {
      val context = contextStore.get()
      return loadInContext(context, key)
    } finally {
      clearContext()
    }
  }

  abstract fun loadInContext(context: C, key: K): V
}
