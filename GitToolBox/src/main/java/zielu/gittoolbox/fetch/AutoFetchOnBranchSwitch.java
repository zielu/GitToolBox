package zielu.gittoolbox.fetch;

import com.intellij.openapi.project.Project;
import git4idea.repo.GitRepository;
import java.time.Duration;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.cache.RepoInfo;
import zielu.gittoolbox.util.AppUtil;

class AutoFetchOnBranchSwitch {
  private final AutoFetchSchedule schedule;
  private final AutoFetchExecutor executor;

  AutoFetchOnBranchSwitch(@NotNull AutoFetchSchedule schedule, @NotNull AutoFetchExecutor executor) {
    this.schedule = schedule;
    this.executor = executor;
  }

  void onBranchSwitch(@NotNull RepoInfo current, @NotNull GitRepository repository) {
    if (current.status().isTrackingRemote()) {
      Duration delay = schedule.calculateTaskDelayOnBranchSwitch();
      if (!delay.isZero()) {
        executor.scheduleTask(delay, repository);
      }
    }
  }

  static AutoFetchOnBranchSwitch getInstance(@NotNull Project project) {
    return AppUtil.getServiceInstance(project, AutoFetchOnBranchSwitch.class);
  }
}
