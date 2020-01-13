package zielu.gittoolbox.ui.projectview

import com.intellij.openapi.project.Project
import git4idea.repo.GitRepository
import zielu.gittoolbox.cache.PerRepoStatusCacheListener
import zielu.gittoolbox.cache.RepoInfo
import zielu.gittoolbox.cache.VirtualFileRepoCacheListener
import zielu.gittoolbox.changes.ChangesTrackerListener
import zielu.gittoolbox.config.AppConfigNotifier
import zielu.gittoolbox.config.GitToolBoxConfig2

internal class ProjectViewSubscriberChangesTrackerListener(private val project: Project) : ChangesTrackerListener {
  override fun changeCountsUpdated() {
    ProjectViewSubscriber.getInstance(project).refreshProjectView()
  }
}

internal class ProjectViewSubscriberConfigListener(private val project: Project) : AppConfigNotifier {
  override fun configChanged(previous: GitToolBoxConfig2, current: GitToolBoxConfig2) {
    ProjectViewSubscriber.getInstance(project).refreshProjectView()
  }
}

internal class ProjectViewSubscriberInfoCacheListener(private val project: Project) : PerRepoStatusCacheListener {
  override fun stateChanged(info: RepoInfo, repository: GitRepository) {
    ProjectViewSubscriber.getInstance(project).refreshProjectView()
  }

  override fun evicted(repositories: Collection<GitRepository>) {
    ProjectViewSubscriber.getInstance(project).refreshProjectView()
  }
}

internal class ProjectViewSubscriberVfCacheListener(private val project: Project) : VirtualFileRepoCacheListener {
  override fun updated() {
    ProjectViewSubscriber.getInstance(project).refreshProjectView()
  }
}
