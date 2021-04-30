package zielu.gittoolbox.branch

import com.intellij.openapi.project.Project
import zielu.gittoolbox.config.GitToolBoxConfig2
import zielu.gittoolbox.config.GitToolBoxConfigPrj
import zielu.gittoolbox.config.ProjectConfig
import zielu.gittoolbox.util.AppUtil

internal class OutdatedBranchesSubscriber(private val project: Project) {

  fun onAllReposInitialized() {
    OutdatedBranchesSchedulerService.getInstance(project).setupSchedule()
  }

  fun onConfigChanged(previous: GitToolBoxConfigPrj, current: GitToolBoxConfigPrj) {
    OutdatedBranchesSchedulerService.getInstance(project).onConfigChanged(
      ProjectConfig.getMerged(previous),
      ProjectConfig.getMerged(current)
    )
  }

  fun onConfigChanged(previous: GitToolBoxConfig2, current: GitToolBoxConfig2) {
    OutdatedBranchesSchedulerService.getInstance(project).onConfigChanged(
      ProjectConfig.getMerged(previous, project),
      ProjectConfig.getMerged(current, project)
    )
  }

  companion object {
    fun getInstance(project: Project): OutdatedBranchesSubscriber {
      return AppUtil.getServiceInstance(project, OutdatedBranchesSubscriber::class.java)
    }
  }
}
