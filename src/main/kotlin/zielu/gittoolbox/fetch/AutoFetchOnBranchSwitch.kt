package zielu.gittoolbox.fetch

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import git4idea.repo.GitRepository
import zielu.gittoolbox.cache.RepoInfo
import zielu.gittoolbox.util.AppUtil.getServiceInstance

internal class AutoFetchOnBranchSwitch(private val project: Project) {

  fun onBranchSwitch(current: RepoInfo, repository: GitRepository) {
    if (current.status.isTrackingRemote) {
      val delay = AutoFetchSchedule.getInstance(project).calculateTaskDelayOnBranchSwitch(repository)
      log.info("Auto-fetch delay on branch switch is $delay")
      if (!delay.isZero) {
        AutoFetchExecutor.getInstance(project).scheduleTask(delay, repository)
      }
    }
  }

  companion object {
    private val log = Logger.getInstance(AutoFetchOnBranchSwitch::class.java)

    @JvmStatic
    fun getInstance(project: Project): AutoFetchOnBranchSwitch {
      return getServiceInstance(project, AutoFetchOnBranchSwitch::class.java)
    }
  }
}
