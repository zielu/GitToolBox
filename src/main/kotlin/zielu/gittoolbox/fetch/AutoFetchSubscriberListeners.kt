package zielu.gittoolbox.fetch

import com.intellij.openapi.project.Project
import git4idea.repo.GitRepository
import zielu.gittoolbox.cache.PerRepoStatusCacheListener
import zielu.gittoolbox.cache.RepoInfo
import zielu.gittoolbox.config.GitToolBoxConfigPrj
import zielu.gittoolbox.config.ProjectConfigNotifier
import zielu.gittoolbox.lifecycle.ProjectLifecycleNotifier

internal class AutoFetchSubscriberConfigListener(private val project: Project) : ProjectConfigNotifier {
  override fun configChanged(previous: GitToolBoxConfigPrj, current: GitToolBoxConfigPrj) {
    AutoFetchSubscriber.getInstance(project).onConfigChanged(previous, current)
  }
}

internal class AutoFetchSubscriberFetchStateListener(private val project: Project) : AutoFetchNotifier {
  override fun stateChanged(state: AutoFetchState) {
    AutoFetchSubscriber.getInstance(project).onStateChanged(state)
  }
}

internal class AutoFetchSubscriberInfoCacheListener(private val project: Project) : PerRepoStatusCacheListener {
  override fun stateChanged(previous: RepoInfo, current: RepoInfo, repository: GitRepository) {
    AutoFetchSubscriber.getInstance(project).onRepoStateChanged(previous, current, repository)
  }

  override fun evicted(repositories: Collection<GitRepository>) {
    AutoFetchSubscriber.getInstance(project).onReposEvicted(repositories)
  }

  override fun allRepositoriesInitialized(repositories: Collection<GitRepository>) {
    AutoFetchSubscriber.getInstance(project).onAllReposInitialized(repositories)
  }
}

internal class AutoFetchSubscriberProjectListener : ProjectLifecycleNotifier {
  override fun projectReady(project: Project) {
    AutoFetchSubscriber.getInstance(project).onProjectReady()
  }
}
