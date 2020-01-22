package zielu.gittoolbox.cache

import com.intellij.dvcs.repo.VcsRepositoryMappingListener
import com.intellij.openapi.project.Project
import git4idea.repo.GitRepository
import git4idea.repo.GitRepositoryChangeListener
import zielu.gittoolbox.config.GitToolBoxConfigPrj
import zielu.gittoolbox.config.ProjectConfigNotifier

internal class CacheSourcesSubscriberConfigListener(private val project: Project) : ProjectConfigNotifier {
  override fun configChanged(previous: GitToolBoxConfigPrj, current: GitToolBoxConfigPrj) {
    CacheSourcesSubscriber.getInstance(project).onConfigChanged(previous, current)
  }
}

internal class CacheSourcesSubscriberGitRepositoryListener : GitRepositoryChangeListener {
  override fun repositoryChanged(repository: GitRepository) {
    CacheSourcesSubscriber.getInstance(repository.project).onRepoChanged(repository)
  }
}

internal class CacheSourcesSubscriberMappingListener(private val project: Project) : VcsRepositoryMappingListener {
  override fun mappingChanged() {
    CacheSourcesSubscriber.getInstance(project).onDirMappingChanged()
  }
}
