package zielu.gittoolbox.branch

import com.intellij.openapi.project.Project
import git4idea.GitBranch
import git4idea.repo.GitRepository
import zielu.gittoolbox.repo.GtRepository
import zielu.gittoolbox.repo.createGtRepository

internal open class BranchSubscriberFacade(private val project: Project) {
  fun branchSwitch(previousBranch: GitBranch, currentBranch: GitBranch, repository: GitRepository) {
    RecentBranchesService.getInstance(project).branchSwitch(previousBranch, currentBranch, toGtRepo(repository))
  }

  fun switchFromBranchToOther(previousBranch: GitBranch, repository: GitRepository) {
    RecentBranchesService.getInstance(project).switchFromBranchToOther(previousBranch, toGtRepo(repository))
  }

  fun switchToBranchFromOther(currentBranch: GitBranch, repository: GitRepository) {
    RecentBranchesService.getInstance(project).switchToBranchFromOther(currentBranch, toGtRepo(repository))
  }

  private fun toGtRepo(repository: GitRepository): GtRepository {
    return createGtRepository(repository)
  }
}
