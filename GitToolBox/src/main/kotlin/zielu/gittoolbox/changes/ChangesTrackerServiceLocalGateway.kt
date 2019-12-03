package zielu.gittoolbox.changes

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.FilePath
import zielu.gittoolbox.cache.VirtualFileRepoCache
import zielu.gittoolbox.changes.ChangesTrackerService.Companion.CHANGES_TRACKER_TOPIC
import zielu.gittoolbox.util.LocalGateway

internal class ChangesTrackerServiceLocalGateway(
  private val project: Project
): LocalGateway(project) {
  private val messageBus by lazy {
    project.messageBus
  }

  fun fireChangesCountUpdated(changesCount: Int) {
    runInBackground { messageBus.syncPublisher(CHANGES_TRACKER_TOPIC).changesCountChanged(changesCount) }
  }

  fun isUnderGit(path: FilePath): Boolean {
    return VirtualFileRepoCache.getInstance(project).isUnderGitRoot(path)
  }
}
