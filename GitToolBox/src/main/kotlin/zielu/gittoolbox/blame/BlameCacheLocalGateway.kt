package zielu.gittoolbox.blame

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsException
import com.intellij.openapi.vcs.history.VcsRevisionNumber
import com.intellij.openapi.vfs.VirtualFile
import git4idea.repo.GitRepository
import zielu.gittoolbox.cache.VirtualFileRepoCache
import zielu.gittoolbox.util.ExecutableTask
import zielu.gittoolbox.util.LocalGateway

internal class BlameCacheLocalGateway(var project: Project) : LocalGateway(project) {
  private val messageBus = project.messageBus

  fun fireBlameUpdated(vFile: VirtualFile, annotation: BlameAnnotation) {
    runInBackground { messageBus.syncPublisher(BlameCache.CACHE_UPDATES).cacheUpdated(vFile, annotation) }
  }

  fun getRepoForFile(vFile: VirtualFile): GitRepository? {
    return VirtualFileRepoCache.getInstance(project).getRepoForFile(vFile)
  }

  fun getBlameLoader(): BlameLoader {
    return BlameLoader.getInstance(project)
  }

  fun execute(task: ExecutableTask) {
    BlameCacheExecutor.getInstance(project).execute(task)
  }

  @Throws(VcsException::class)
  fun getCurrentRevision(repository: GitRepository): VcsRevisionNumber? {
    return getBlameLoader().getCurrentRevision(repository)
  }
}
