package zielu.gittoolbox.blame.calculator

import com.google.common.cache.CacheBuilder
import com.google.common.cache.LoadingCache
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.history.VcsRevisionNumber
import com.intellij.openapi.vfs.VirtualFile
import git4idea.repo.GitRepository
import zielu.gittoolbox.revision.RevisionDataProvider
import zielu.intellij.guava.ThreadLocalCacheLoader
import zielu.intellij.guava.getSafe

internal class CachingBlameCalculator(project: Project) : BlameCalculator {
  private val gateway = CachingBlameCalculatorLocalGateway(project)
  private val loader = object : ThreadLocalCacheLoader<LoadContext, Key, RevisionDataProvider>() {
    override fun loadInContext(context: LoadContext, key: Key): RevisionDataProvider {
      return calculate(context.repository, context.file, key.revision)
    }
  }
  private val dataProviders: LoadingCache<Key, RevisionDataProvider> = CacheBuilder.newBuilder()
    .maximumSize(50)
    .recordStats()
    .build(loader)

  init {
    gateway.exposeCacheMetrics(dataProviders)
  }

  override fun annotate(
    repository: GitRepository,
    file: VirtualFile,
    revision: VcsRevisionNumber
  ): RevisionDataProvider? {
    val key = Key(file.url, revision)
    loader.setContext(LoadContext(repository, file))
    val presentProvider = dataProviders.getIfPresent(key)
    if (presentProvider == RevisionDataProvider.EMPTY) {
      dataProviders.invalidate(key)
    } else if (presentProvider != null) {
      loader.clearContext()
      return presentProvider
    }
    val provider = dataProviders.getSafe(key, RevisionDataProvider.EMPTY)
    loader.clearContext()
    return toResult(provider)
  }

  private fun toResult(provider: RevisionDataProvider): RevisionDataProvider? {
    return if (provider == RevisionDataProvider.EMPTY) {
      null
    } else {
      provider
    }
  }

  private fun calculate(
    repository: GitRepository,
    file: VirtualFile,
    revision: VcsRevisionNumber
  ): RevisionDataProvider {
    return getFromPersistence(file, revision)?.apply {
      log.debug("Found persisted blame provider for ", file, " at ", revision)
    } ?: gateway.calculator().annotate(repository, file, revision)?.apply {
      log.debug("Calculated blame for ", file, " at ", revision)
      storeInPersistence(this)
    } ?: RevisionDataProvider.EMPTY
  }

  private fun getFromPersistence(file: VirtualFile, revision: VcsRevisionNumber): RevisionDataProvider? {
    return if (gateway.shouldLoadFromPersistence()) {
      gateway.loadFromPersistence(file, revision)
    } else {
      null
    }
  }

  private fun storeInPersistence(dataProvider: RevisionDataProvider?) {
    if (gateway.shouldLoadFromPersistence()) {
      dataProvider?.apply {
        if (this != RevisionDataProvider.EMPTY) {
          gateway.storeInPersistence(this)
        }
      }
    }
  }

  override fun invalidateForRoot(root: VirtualFile) {
    val keys = dataProviders.asMap().keys.toMutableSet()
    val rootUrl = root.url
    val keysToInvalidate = keys.filter {
      it.url.startsWith(rootUrl)
    }
    log.debug("Invalidate ", keysToInvalidate)
    dataProviders.invalidateAll(keysToInvalidate)
    gateway.calculator().invalidateForRoot(root)
  }

  private companion object {
    private val log = Logger.getInstance(CachingBlameCalculator::class.java)
  }
}

private data class Key(
  val url: String,
  val revision: VcsRevisionNumber
)

private data class LoadContext(
  val repository: GitRepository,
  val file: VirtualFile
)
