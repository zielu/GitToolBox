package zielu.gittoolbox.cache

import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic
import git4idea.repo.GitRepository
import zielu.gittoolbox.util.AppUtil.getServiceInstance

internal interface PerRepoInfoCache : DirMappingAware, RepoChangeAware {
  fun getInfo(repository: GitRepository): RepoInfo

  fun getAllInfos(): List<RepoInfo>

  fun refreshAll()

  fun refresh(repositories: Iterable<GitRepository>)

  companion object {
    @JvmField
    val CACHE_CHANGE_TOPIC = Topic.create("Status cache change", PerRepoStatusCacheListener::class.java)

    @JvmStatic
    fun getInstance(project: Project): PerRepoInfoCache {
      return getServiceInstance(project, PerRepoInfoCache::class.java)
    }
  }
}
