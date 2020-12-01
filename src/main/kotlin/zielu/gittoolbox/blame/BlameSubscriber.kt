package zielu.gittoolbox.blame

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import git4idea.repo.GitRepository
import zielu.gittoolbox.util.AppUtil

internal class BlameSubscriber(private val project: Project) {

  fun onRepoStateChanged(repository: GitRepository) {
    BlameCache.getExistingInstance(project).ifPresent { cache: BlameCache ->
      cache.refreshForRoot(repository.root)
    }
  }

  fun onCacheUpdated(file: VirtualFile, annotation: BlameAnnotation) {
    BlameService.getExistingInstance(project).ifPresent { service: BlameService ->
      service.blameUpdated(file, annotation)
    }
  }

  fun onCacheInvalidated(file: VirtualFile) {
    BlameService.getExistingInstance(project).ifPresent { service: BlameService ->
      service.invalidate(file)
    }
  }

  companion object {
    fun getInstance(project: Project): BlameSubscriber {
      return AppUtil.getServiceInstance(project, BlameSubscriber::class.java)
    }
  }
}
