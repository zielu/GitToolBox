package zielu.gittoolbox.branch

import zielu.gittoolbox.repo.GtRepository
import zielu.gittoolbox.store.RecentBranch
import zielu.gittoolbox.store.WorkspaceStore
import java.time.Instant

internal open class RecentBranchesFacade {

  fun getRecentBranchesFromStore(repository: GtRepository): List<RecentBranch> {
    val store = WorkspaceStore.get(repository.project)
    return store.recentBranches.findForRepositoryRootUrl(repository.getRootUrl())
  }

  fun storeRecentBranches(recentBranches: List<RecentBranch>, repository: GtRepository) {
    val store = WorkspaceStore.get(repository.project)
    store.recentBranches.storeForRepositoryRootUrl(recentBranches, repository.getRootUrl())
  }

  fun now(): Instant {
    return Instant.now()
  }
}
