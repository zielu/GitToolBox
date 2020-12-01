package zielu.gittoolbox.ui.branch

import git4idea.GitBranch
import git4idea.branch.GitBrancher
import zielu.gittoolbox.repo.GtRepository

internal interface RecentBranchItem {
  fun getText(): String
  fun getSeparatorText(): String?
  fun onChosen()
}

internal fun createRecentBranchItem(branch: GitBranch, repository: GtRepository): RecentBranchItem {
  return RecentBranchInRepoItem(branch, repository, false)
}

internal fun createTitledRecentBranchItem(branch: GitBranch, repository: GtRepository): RecentBranchItem {
  return RecentBranchInRepoItem(branch, repository, true)
}

private data class RecentBranchInRepoItem(
  private val branch: GitBranch,
  private val repository: GtRepository,
  private val firstRepoItem: Boolean
) : RecentBranchItem {

  override fun getText(): String {
    return branch.name
  }

  override fun getSeparatorText(): String? {
    if (firstRepoItem) {
      return repository.getName()
    }
    return null
  }

  override fun onChosen() {
    val brancher = GitBrancher.getInstance(repository.project)
    brancher.checkout(branch.name, false, arrayListOf(repository), null)
  }
}
