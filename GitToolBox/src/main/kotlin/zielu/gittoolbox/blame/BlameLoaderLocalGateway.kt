package zielu.gittoolbox.blame

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsException
import com.intellij.openapi.vcs.history.VcsRevisionNumber
import com.intellij.openapi.vfs.VirtualFile
import git4idea.GitVcs
import git4idea.repo.GitRepository
import zielu.gittoolbox.cache.VirtualFileRepoCache
import zielu.gittoolbox.revision.RevisionService

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

  fun annotationLock(vFile: VirtualFile) {
    BlameUtil.annotationLock(project, vFile)
  }

  fun annotationUnlock(vFile: VirtualFile) {
    BlameUtil.annotationUnlock(project, vFile)
  }

  companion object {
    private val log = Logger.getInstance(BlameLoaderLocalGateway::class.java)
  }
}
