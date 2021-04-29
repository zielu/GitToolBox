package zielu.gittoolbox.branch

import com.intellij.openapi.project.Project
import git4idea.repo.GitRepository
import zielu.gittoolbox.cache.PerRepoStatusCacheListener

internal class OutdatedBranchesListener(
  private val project: Project
) : PerRepoStatusCacheListener {
  override fun allRepositoriesInitialized(repositories: Collection<GitRepository>) {
    OutdatedBranchesSchedulerService.getInstance(project).setupSchedule()
  }
}
