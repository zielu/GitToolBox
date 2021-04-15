package zielu.gittoolbox.branch

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import git4idea.repo.GitRepository
import zielu.gittoolbox.cache.VirtualFileRepoCache
import zielu.intellij.concurrent.ZCompletableBackgroundable

internal class OutdatedBranchesCollector(
  myProject: Project
) : ZCompletableBackgroundable<Map<GitRepository, List<OutdatedBranch>>>(
  myProject,
  "Collect outdated branches"
) {
  private lateinit var data: Map<GitRepository, List<OutdatedBranch>>

  override fun run(indicator: ProgressIndicator) {
    val repositories = VirtualFileRepoCache.getInstance(project).repositories
    val outdatedBranchesService = OutdatedBranchesService.getInstance(project)

    data = repositories.associateWith {
      indicator.checkCanceled()
      outdatedBranchesService.outdatedBranches(it)
    }.filter { it.value.isNotEmpty() }
  }

  override fun getResult(): Map<GitRepository, List<OutdatedBranch>> = data
}
