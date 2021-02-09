package zielu.gittoolbox.revision

import com.google.common.cache.CacheBuilder
import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.history.VcsRevisionNumber
import com.intellij.openapi.vfs.VirtualFile
import zielu.gittoolbox.util.AppUtil
import java.time.Duration
import java.util.concurrent.ExecutionException

internal class RevisionService(
  private val project: Project
) : Disposable {
  private val commitMessageCache = CacheBuilder.newBuilder()
    .maximumSize(50)
    .expireAfterAccess(Duration.ofMinutes(30))
    .recordStats()
    .build<VcsRevisionNumber, String>()
  private val facade = RevisionServiceFacade(project)
  init {
    facade.exposeCommitMessageCacheMetrics(commitMessageCache)
  }

  override fun dispose() {
    commitMessageCache.invalidateAll()
  }

  fun getForLine(provider: RevisionDataProvider, lineNumber: Int): RevisionInfo {
    return RevisionInfoFactory.getInstance(project).forLine(provider, lineNumber)
  }

  fun getCommitMessage(file: VirtualFile, revisionInfo: RevisionInfo): String? {
    if (revisionInfo.isEmpty()) {
      return null
    }
    val revisionNumber = revisionInfo.getRevisionNumber()
    return try {
      commitMessageCache[revisionNumber, { facade.loadCommitMessage(file, revisionNumber) }]
    } catch (e: ExecutionException) {
      log.warn("Failed to load commit message $revisionNumber", e)
      null
    }
  }

  companion object {
    private val log = Logger.getInstance(RevisionService::class.java)

    @JvmStatic
    fun getInstance(project: Project): RevisionService {
      return AppUtil.getServiceInstance(project, RevisionService::class.java)
    }
  }
}
