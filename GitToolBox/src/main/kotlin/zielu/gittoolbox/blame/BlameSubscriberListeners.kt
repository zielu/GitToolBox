package zielu.gittoolbox.blame

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import git4idea.repo.GitRepository
import zielu.gittoolbox.cache.PerRepoStatusCacheListener
import zielu.gittoolbox.cache.RepoInfo

internal class BlameSubscriberCacheListener(private val project: Project) : BlameCacheListener {
  override fun cacheUpdated(file: VirtualFile, annotation: BlameAnnotation) {
    BlameSubscriber.getInstance(project).onCacheUpdated(file, annotation)
  }

  override fun invalidated(file: VirtualFile) {
    BlameSubscriber.getInstance(project).onCacheInvalidated(file)
  }
}

internal class BlameSubscriberInfoCacheListener : PerRepoStatusCacheListener {
  override fun stateChanged(info: RepoInfo, repository: GitRepository) {
    BlameSubscriber.getInstance(repository.project).onRepoStateChanged(repository)
  }
}
