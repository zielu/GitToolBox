package zielu.gittoolbox.blame

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.history.VcsRevisionNumber
import com.intellij.openapi.vfs.VirtualFile
import git4idea.GitVcs
import git4idea.repo.GitRepository
import zielu.gittoolbox.cache.VirtualFileRepoCache
import zielu.gittoolbox.metrics.ProjectMetrics
import zielu.gittoolbox.revision.RevisionService
import zielu.gittoolbox.util.GtUtil
import zielu.gittoolbox.util.BaseFacade

internal class BlameLoaderFacade(private val project: Project) : BaseFacade(project) {
  fun getRepoForFile(vFile: VirtualFile): GitRepository? {
    return VirtualFileRepoCache.getInstance(project).getRepoForFile(vFile)
  }

  fun getRevisionService(): RevisionService {
    return RevisionService.getInstance(project)
  }

  fun getCurrentRevisionNumber(repo: GitRepository): VcsRevisionNumber {
    return try {
      GitVcs.getInstance(project).parseRevisionNumber(repo.currentRevision) ?: VcsRevisionNumber.NULL
    } catch (e: Exception) {
      log.warn("Could not get current repoRevision for " + repo.root, e)
      VcsRevisionNumber.NULL
    }
  }

  fun getCurrentRevisionNumber(vFile: VirtualFile): VcsRevisionNumber {
    val timer = ProjectMetrics.getInstance(project).timer("blame-loader.current-version")
    return timer.timeSupplierKt { GtUtil.getCurrentRevision(project, vFile) }
  }

  companion object {
    private val log = Logger.getInstance(BlameLoaderFacade::class.java)
  }
}
