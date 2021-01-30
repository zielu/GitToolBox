package zielu.gittoolbox

import com.intellij.openapi.util.registry.Registry

internal object GitToolBoxRegistry {
  fun shouldLoadBlameFromPersistence(): Boolean {
    return Registry.`is`("zielu.gittoolbox.blame.cache.persistent", true)
  }

  @JvmStatic
  fun shouldDebounceFirstAutoFetch(): Boolean {
    return Registry.`is`("zielu.gittoolbox.fetch.auto.debounce.first", false)
  }

  @JvmStatic
  fun shouldNotDebounceFirstAutoFetch(): Boolean {
    return !shouldDebounceFirstAutoFetch()
  }

  fun diagnosticMode(): Boolean {
    return Registry.`is`("zielu.gittoolbox.diagnostic.mode", false)
  }
}
