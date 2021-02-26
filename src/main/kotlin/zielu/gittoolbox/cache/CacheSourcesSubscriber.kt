package zielu.gittoolbox.cache

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import git4idea.repo.GitRepository
import zielu.gittoolbox.config.MergedProjectConfig
import zielu.gittoolbox.util.AppUtil
import zielu.gittoolbox.util.GtUtil

internal class CacheSourcesSubscriber(private val project: Project) {
  private val dirMappingAwares: List<DirMappingAware>
  private val repoChangeAwares: List<RepoChangeAware>

  init {
    dirMappingAwares = listOf(
      LazyDirMappingAware { VirtualFileRepoCache.getInstance(project) },
      LazyDirMappingAware { PerRepoInfoCache.getInstance(project) }
    )
    repoChangeAwares = listOf(
      LazyRepoChangeAware { PerRepoInfoCache.getInstance(project) }
    )
  }

  fun onRepoChanged(repository: GitRepository) {
    log.debug("Repo changed: ", repository)
    repoChangeAwares.forEach {
      it.repoChanged(repository)
    }
    log.debug("Repo changed notification done: ", repository)
  }

  fun onDirMappingChanged() {
    log.info("Dir mappings changed")
    val repositories = GtUtil.getRepositories(project)
    dirMappingAwares.forEach {
      it.updatedRepoList(repositories)
    }
    log.debug("Dir mappings change notification done")
  }

  fun onConfigChanged(previous: MergedProjectConfig, current: MergedProjectConfig) {
    if (previous.isReferencePointForStatusChanged(current)) {
      GtUtil.getRepositories(project).forEach { repo ->
        repoChangeAwares.forEach { aware: RepoChangeAware ->
          aware.repoChanged(repo)
        }
      }
    }
  }

  companion object {
    private val log = Logger.getInstance(CacheSourcesSubscriber::class.java)

    fun getInstance(project: Project): CacheSourcesSubscriber {
      return AppUtil.getServiceInstance(project, CacheSourcesSubscriber::class.java)
    }
  }
}
