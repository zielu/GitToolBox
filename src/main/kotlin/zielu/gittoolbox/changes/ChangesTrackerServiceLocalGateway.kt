package zielu.gittoolbox.changes

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.FilePath
import git4idea.repo.GitRepository
import zielu.gittoolbox.cache.VirtualFileRepoCache
import zielu.gittoolbox.changes.ChangesTrackerService.Companion.CHANGES_TRACKER_TOPIC
import zielu.gittoolbox.util.LocalGateway
import zielu.intellij.metrics.GtTimer

internal open class ChangesTrackerServiceLocalGateway(
  private val project: Project
) : LocalGateway(project), Disposable {

  fun fireChangeCountsUpdated() {
    publishAsync(this) { it.syncPublisher(CHANGES_TRACKER_TOPIC).changeCountsUpdated() }
  }

  fun getRepoForPath(path: FilePath): GitRepository? {
    return VirtualFileRepoCache.getInstance(project).getRepoForPath(path)
  }

  fun getNotEmptyChangeListTimer(): GtTimer = getMetrics().timer("change-list-not-empty")

  fun getChangeListRemovedTimer(): GtTimer = getMetrics().timer("change-list-removed")

  override fun dispose() {
    // TODO: implement
  }
}
