package zielu.gittoolbox.blame.calculator

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.history.VcsFileRevision
import com.intellij.openapi.vcs.history.VcsRevisionNumber
import com.intellij.openapi.vfs.VirtualFile
import git4idea.repo.GitRepository
import zielu.gittoolbox.revision.RevisionDataProvider

internal class CachingBlameCalculator(project: Project) : BlameCalculator {
  private val calculator = IncrementalBlameCalculator(project)
  private val dataProviders: Cache<Key, RevisionDataProvider> = CacheBuilder.newBuilder()
    .maximumSize(50)
    .build()

  override fun annotate(
    repository: GitRepository,
    file: VirtualFile,
    revision: VcsRevisionNumber
  ): RevisionDataProvider? {
    if (revision != VcsFileRevision.NULL) {
      val key = Key(file.url, revision)
      var dataProvider = dataProviders.getIfPresent(key)
      return if (dataProvider == null) {
        log.debug("Missing cached blame provider for ", key)
        dataProvider = calculator.annotate(repository, file, revision)
        if (dataProvider != null) {
          dataProviders.put(key, dataProvider)
        }
        dataProvider
      } else {
        log.debug("Found cached blame provider for ", key)
        dataProvider
      }
    } else {
      return calculator.annotate(repository, file, revision)
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
    calculator.invalidateForRoot(root)
  }

  private companion object {
    private val log = Logger.getInstance(CachingBlameCalculator::class.java)
  }
}

private data class Key(
  val url: String,
  val revision: VcsRevisionNumber
)
