package zielu.gittoolbox.ui.statusbar

import com.intellij.openapi.project.Project
import git4idea.repo.GitRepository
import zielu.gittoolbox.cache.PerRepoInfoCache
import zielu.gittoolbox.cache.RepoInfo
import zielu.gittoolbox.config.ProjectConfig
import zielu.gittoolbox.fetch.AutoFetchComponent
import zielu.gittoolbox.util.GtUtil

internal class StatusToolTipFacade(private val project: Project) {

  fun getRepositories(): List<GitRepository> = GtUtil.getRepositories(project)

  fun getRepoInfo(repo: GitRepository): RepoInfo = PerRepoInfoCache.getInstance(project).getInfo(repo)

  fun isAutoFetchEnabled(): Boolean = ProjectConfig.get(project).autoFetch

  fun getLastAutoFetchTimestamp(): Long = AutoFetchComponent.getInstance(project).lastAutoFetch()
}
