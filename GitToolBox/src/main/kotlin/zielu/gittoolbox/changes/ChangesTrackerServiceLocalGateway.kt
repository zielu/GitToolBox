package zielu.gittoolbox.changes

import com.codahale.metrics.Timer
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.FilePath
import git4idea.repo.GitRepository
import zielu.gittoolbox.cache.VirtualFileRepoCache
import zielu.gittoolbox.changes.ChangesTrackerService.Companion.CHANGES_TRACKER_TOPIC
import zielu.gittoolbox.util.LocalGateway

internal class ChangesTrackerServiceLocalGateway(
  private val project: Project
) : LocalGateway(project) {
  private val messageBus by lazy {
    project.messageBus
  }

  fun fireChangeCountsUpdated() {
    runInBackground { messageBus.syncPublisher(CHANGES_TRACKER_TOPIC).changeCountsUpdated() }
  }

  fun getRepoForPath(path: FilePath): GitRepository? {
    return VirtualFileRepoCache.getInstance(project).getRepoForPath(path)
  }

  fun getNotEmptyChangeListTimer(): Timer = getMetrics().timer("change-list-not-empty")

  fun getChangeListRemovedTimer(): Timer = getMetrics().timer("change-list-removed")
}
