package zielu.gittoolbox.fetch;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import git4idea.repo.GitRepository;
import java.time.Duration;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.cache.RepoInfo;
import zielu.gittoolbox.util.AppUtil;

class AutoFetchOnBranchSwitch {
  private final Logger log = Logger.getInstance(getClass());
  private final Project project;

  AutoFetchOnBranchSwitch(@NotNull Project project) {
    this.project = project;
  }

  void onBranchSwitch(@NotNull RepoInfo current, @NotNull GitRepository repository) {
    if (current.status().isTrackingRemote()) {
      Duration delay = AutoFetchSchedule.getInstance(project).calculateTaskDelayOnBranchSwitch(repository);
      log.info("Auto-fetch delay on branch switch is " + delay);
      if (!delay.isZero()) {
        AutoFetchExecutor.getInstance(project).scheduleTask(delay, repository);
      }
    }
  }

  @NotNull
  static AutoFetchOnBranchSwitch getInstance(@NotNull Project project) {
    return AppUtil.getServiceInstance(project, AutoFetchOnBranchSwitch.class);
  }

  @NotNull
  static Optional<AutoFetchOnBranchSwitch> getExistingInstance(@NotNull Project project) {
    return AppUtil.getExistingServiceInstance(project, AutoFetchOnBranchSwitch.class);
  }
}
