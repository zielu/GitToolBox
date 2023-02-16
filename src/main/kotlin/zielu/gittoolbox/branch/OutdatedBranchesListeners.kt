package zielu.gittoolbox.branch

import com.intellij.openapi.project.Project
import git4idea.repo.GitRepository
import zielu.gittoolbox.cache.PerRepoStatusCacheListener
import zielu.gittoolbox.config.AppConfigNotifier
import zielu.gittoolbox.config.GitToolBoxConfig2
import zielu.gittoolbox.config.GitToolBoxConfigPrj
import zielu.gittoolbox.config.ProjectConfigNotifier

internal class OutdatedBranchesReposListener(
  private val project: Project
) : PerRepoStatusCacheListener {

  override fun allRepositoriesInitialized(repositories: Collection<GitRepository>) {
    OutdatedBranchesSubscriber.getInstance(project).onAllReposInitialized()
  }
}

internal class OutdatedBranchesPrjConfigListener(private val project: Project) : ProjectConfigNotifier {
  override fun configChanged(previous: GitToolBoxConfigPrj, current: GitToolBoxConfigPrj) {
    OutdatedBranchesSubscriber.getInstance(project).onConfigChanged(previous, current)
  }
}

internal class OutdatedBranchesAppConfigListener(private val project: Project) : AppConfigNotifier {
  override fun configChanged(previous: GitToolBoxConfig2, current: GitToolBoxConfig2) {
    OutdatedBranchesSubscriber.getInstance(project).onConfigChanged(previous, current)
  }
}
