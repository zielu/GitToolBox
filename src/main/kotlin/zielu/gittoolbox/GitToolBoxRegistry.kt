package zielu.gittoolbox

import com.intellij.openapi.util.registry.Registry

internal object GitToolBoxRegistry {
  fun shouldLoadBlameFromPersistence(): Boolean {
    return Registry.`is`("zielu.gittoolbox.blame.cache.persistent", true)
  }

  @JvmStatic
  fun runAutoFetchInBackground(): Boolean {
    return Registry.`is`("zielu.gittoolbox.fetch.auto.in.background", false)
  }

  fun diagnosticMode(): Boolean {
    return Registry.`is`("zielu.gittoolbox.diagnostic.mode", false)
  }

  fun useLegacyConfig(): Boolean {
    return Registry.`is`("zielu.gittoolbox.config.legacy", false)
  }

  fun useNewConfig(): Boolean = !useLegacyConfig()
}
