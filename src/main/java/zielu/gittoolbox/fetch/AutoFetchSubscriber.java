package zielu.gittoolbox.fetch;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import git4idea.repo.GitRepository;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.cache.RepoInfo;
import zielu.gittoolbox.config.GitToolBoxConfig2;
import zielu.gittoolbox.config.GitToolBoxConfigPrj;
import zielu.gittoolbox.config.MergedProjectConfig;
import zielu.gittoolbox.config.ProjectConfig;
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

  public void onAllReposInitialized(@NotNull Collection<? extends GitRepository> repositories) {
    AutoFetchComponent.getInstance(project).allRepositoriesInitialized(repositories.size());
  }

  void onRepoStateChanged(@NotNull RepoInfo previous,
                          @NotNull RepoInfo current,
                          @NotNull GitRepository repository) {
    if (ProjectConfig.getMerged(project).autoFetchOnBranchSwitch()) {
      if (!previous.isEmpty() && !current.isEmpty() && !previous.getStatus().sameLocalBranch(current.getStatus())) {
        if (exclusions.isAllowed(repository)) {
          AutoFetchOnBranchSwitch.getInstance(project).onBranchSwitch(current, repository);
        }
      } else {
        log.debug("Branch switch not eligible for auto-fetch: previous=", previous, ", current=", current);
      }
    } else {
      log.debug("Auto-fetch on branch switch disabled for project: ", project);
    }
  }

  void onConfigChanged(@NotNull GitToolBoxConfig2 previous, @NotNull GitToolBoxConfig2 current) {
    MergedProjectConfig previousMerged = ProjectConfig.getMerged(previous, project);
    MergedProjectConfig currentMerged = ProjectConfig.getMerged(current, project);
    AutoFetchComponent.getInstance(project).configChanged(previousMerged, currentMerged);
  }

  void onConfigChanged(@NotNull GitToolBoxConfigPrj previous, @NotNull GitToolBoxConfigPrj current) {
    MergedProjectConfig previousMerged = ProjectConfig.getMerged(previous);
    MergedProjectConfig currentMerged = ProjectConfig.getMerged(current);
    AutoFetchComponent.getInstance(project).configChanged(previousMerged, currentMerged);
  }

  void onStateChanged(@NotNull AutoFetchState state) {
    AutoFetchComponent.getInstance(project).stateChanged(state);
  }

  void onReposEvicted(@NotNull Collection<GitRepository> repositories) {
    AutoFetchSchedule.getExistingServiceInstance(project)
        .ifPresent(service -> service.repositoriesRemoved(repositories));
  }
}
