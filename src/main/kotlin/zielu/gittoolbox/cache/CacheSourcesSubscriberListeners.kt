package zielu.gittoolbox.cache

import com.intellij.dvcs.repo.VcsRepositoryMappingListener
import com.intellij.openapi.project.Project
import git4idea.repo.GitRepository
import git4idea.repo.GitRepositoryChangeListener
import zielu.gittoolbox.config.AppConfigNotifier
import zielu.gittoolbox.config.GitToolBoxConfig2
import zielu.gittoolbox.config.GitToolBoxConfigPrj
import zielu.gittoolbox.config.ProjectConfig
import zielu.gittoolbox.config.ProjectConfigNotifier
import zielu.gittoolbox.util.MessageBusListener
import zielu.gittoolbox.util.ProjectMessageBusListener

internal class CacheSourcesSubscriberPrjConfigListener(private val project: Project) : ProjectConfigNotifier {
  override fun configChanged(previous: GitToolBoxConfigPrj, current: GitToolBoxConfigPrj) {
    val previousMerged = ProjectConfig.getMerged(previous)
    val currentMerged = ProjectConfig.getMerged(current)
    CacheSourcesSubscriber.getInstance(project).onConfigChanged(previousMerged, currentMerged)
  }
}

internal class CacheSourcesSubscriberAppConfigListener(private val project: Project) : AppConfigNotifier {
  override fun configChanged(previous: GitToolBoxConfig2, current: GitToolBoxConfig2) {
    val previousMerged = ProjectConfig.getMerged(previous, project)
    val currentMerged = ProjectConfig.getMerged(current, project)
    CacheSourcesSubscriber.getInstance(project).onConfigChanged(previousMerged, currentMerged)
  }
}

internal class CacheSourcesSubscriberGitRepositoryListener : MessageBusListener(), GitRepositoryChangeListener {
  override fun repositoryChanged(repository: GitRepository) {
    handleEvent(repository.project) { project ->
      CacheSourcesSubscriber.getInstance(project).onRepoChanged(repository)
    }
  }
}

internal class CacheSourcesSubscriberMappingListener(
  project: Project
) : ProjectMessageBusListener(project), VcsRepositoryMappingListener {
  override fun mappingChanged() {
    handleEvent { project ->
      CacheSourcesSubscriber.getInstance(project).onDirMappingChanged()
    }
  }
}
