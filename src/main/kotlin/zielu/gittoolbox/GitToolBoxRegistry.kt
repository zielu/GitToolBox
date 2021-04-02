package zielu.gittoolbox

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.registry.Registry

internal object GitToolBoxRegistry {
  private const val defaultBlameCalculatorCacheSize = 15
  private const val defaultBlameCacheSize = 30

  private val log = Logger.getInstance(GitToolBoxRegistry::class.java)

  fun shouldLoadBlameFromPersistence(): Boolean {
    return Registry.`is`("zielu.gittoolbox.blame.cache.persistent", true)
  }

  fun blameCacheCalculatorEntriesCount(): Long {
    val count = Registry
      .intValue("zielu.gittoolbox.blame.calculator.cache.size", defaultBlameCalculatorCacheSize)
      .toLong()
    return if (count < 0) {
      log.warn("Invalid value for zielu.gittoolbox.blame.calculator.cache.size = $count")
      defaultBlameCalculatorCacheSize.toLong()
    } else {
      count
    }
  }

  fun blameCacheEntriesCount(): Long {
    val count = Registry
      .intValue("zielu.gittoolbox.blame.cache.size", defaultBlameCacheSize)
      .toLong()
    return if (count < 0) {
      log.warn("Invalid value for zielu.gittoolbox.blame.cache.size = $count")
      defaultBlameCacheSize.toLong()
    } else {
      count
    }
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
