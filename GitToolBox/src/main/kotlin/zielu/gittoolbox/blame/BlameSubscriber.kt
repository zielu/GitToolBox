package zielu.gittoolbox.blame

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import git4idea.repo.GitRepository
import zielu.gittoolbox.cache.PerRepoInfoCache
import zielu.gittoolbox.cache.PerRepoStatusCacheListener
import zielu.gittoolbox.cache.RepoInfo

internal class BlameSubscriber(project: Project) {
  init {
    val connection = project.messageBus.connect(project)
    connection.subscribe(PerRepoInfoCache.CACHE_CHANGE, object : PerRepoStatusCacheListener {
      override fun stateChanged(info: RepoInfo, repository: GitRepository) {
        BlameCache.getExistingInstance(project).ifPresent {
          cache: BlameCache -> cache.refreshForRoot(repository.root)
        }
      }
    })
    connection.subscribe(BlameCache.CACHE_UPDATES, object : BlameCacheListener {
      override fun cacheUpdated(file: VirtualFile, annotation: BlameAnnotation) {
        BlameService.getExistingInstance(project).ifPresent {
          service: BlameService -> service.blameUpdated(file, annotation)
        }
      }

      override fun invalidated(file: VirtualFile) {
        BlameService.getExistingInstance(project).ifPresent {
          service: BlameService -> service.invalidate(file)
        }
      }
    })
  }
}
