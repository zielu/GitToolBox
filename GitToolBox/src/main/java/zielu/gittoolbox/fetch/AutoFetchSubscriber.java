package zielu.gittoolbox.fetch;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import git4idea.repo.GitRepository;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.cache.RepoInfo;
import zielu.gittoolbox.config.GitToolBoxConfigPrj;
import zielu.gittoolbox.util.AppUtil;

class AutoFetchSubscriber {
  private final Logger log = Logger.getInstance(getClass());
  private final Project project;
  private final AutoFetchExclusions exclusions;

  AutoFetchSubscriber(@NotNull Project project) {
    this.project = project;
    exclusions = new AutoFetchExclusions(project);
  }

  @NotNull
  static AutoFetchSubscriber getInstance(@NotNull Project project) {
    return AppUtil.getServiceInstance(project, AutoFetchSubscriber.class);
  }

  void onProjectReady() {
    AutoFetch.getInstance(project).projectReady();
  }

  void onRepoStateChanged(@NotNull RepoInfo previous,
                          @NotNull RepoInfo current,
                          @NotNull GitRepository repository) {
    if (GitToolBoxConfigPrj.getInstance(project).getAutoFetchOnBranchSwitch()) {
      if (!previous.isEmpty() && !current.isEmpty() && !previous.status().sameLocalBranch(current.status())) {
        if (exclusions.isAllowed(repository)) {
          AutoFetchOnBranchSwitch.getInstance(project).onBranchSwitch(current, repository);
        }
      } else {
        log.info("Branch switch not eligible for auto-fetch: previous=" + previous + ", current=" + current);
      }
    }
  }

  void onConfigChanged(@NotNull GitToolBoxConfigPrj previous, @NotNull GitToolBoxConfigPrj current) {
    AutoFetchComponent.getInstance(project).configChanged(previous, current);
  }

  void onStateChanged(@NotNull AutoFetchState state) {
    AutoFetchComponent.getInstance(project).stateChanged(state);
  }

  void onReposEvicted(@NotNull Collection<GitRepository> repositories) {
    AutoFetchSchedule.getExistingServiceInstance(project)
        .ifPresent(service -> service.repositoriesRemoved(repositories));
  }
}
