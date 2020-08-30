package zielu.gittoolbox.blame

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsException
import com.intellij.openapi.vcs.history.VcsRevisionNumber
import com.intellij.openapi.vfs.VirtualFile
import git4idea.GitVcs
import git4idea.repo.GitRepository
import zielu.gittoolbox.blame.persistence.PersistentBlameCache
import zielu.gittoolbox.cache.VirtualFileRepoCache
import zielu.gittoolbox.metrics.ProjectMetrics
import zielu.gittoolbox.revision.RevisionDataProvider
import zielu.gittoolbox.revision.RevisionService
import zielu.gittoolbox.util.GtUtil
import java.util.Optional

internal class BlameLoaderLocalGateway(private val project: Project) {
  fun getRepoForFile(vFile: VirtualFile): GitRepository? {
    return VirtualFileRepoCache.getInstance(project).getRepoForFile(vFile)
  }

  fun getRevisionService(): RevisionService {
    return RevisionService.getInstance(project)
  }

  fun getCurrentRevisionNumber(repo: GitRepository): VcsRevisionNumber {
    return try {
      GitVcs.getInstance(project).parseRevisionNumber(repo.currentRevision) ?: VcsRevisionNumber.NULL
    } catch (e: VcsException) {
      log.warn("Could not get current repoRevision for " + repo.root, e)
      VcsRevisionNumber.NULL
    }
  }

  fun getCurrentRevisionNumber(vFile: VirtualFile): VcsRevisionNumber {
    val timer = ProjectMetrics.getInstance(project).timer("blame-loader.current-version")
    return timer.timeSupplier { GtUtil.getCurrentRevision(project, vFile) }
  }

  fun getCachedData(vFile: VirtualFile, revision: VcsRevisionNumber): Optional<RevisionDataProvider> {
    val persistentCache = PersistentBlameCache.getInstance(project)
    return persistentCache.getBlame(vFile, revision)
  }

  fun cacheData(dataProvider: RevisionDataProvider) {
    val persistentCache = PersistentBlameCache.getInstance(project)
    persistentCache.storeBlame(dataProvider)
  }

  companion object {
    private val log = Logger.getInstance(BlameLoaderLocalGateway::class.java)
  }
}
