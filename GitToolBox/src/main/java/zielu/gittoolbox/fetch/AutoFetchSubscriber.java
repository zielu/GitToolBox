package zielu.gittoolbox.fetch;

import com.google.common.base.Preconditions;
import com.intellij.openapi.components.BaseComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import git4idea.repo.GitRepository;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.cache.PerRepoInfoCache;
import zielu.gittoolbox.cache.PerRepoStatusCacheListener;
import zielu.gittoolbox.cache.RepoInfo;
import zielu.gittoolbox.config.ConfigNotifier;
import zielu.gittoolbox.config.GitToolBoxConfigPrj;

class AutoFetchSubscriber implements BaseComponent {
  private final Logger log = Logger.getInstance(getClass());
  private final Project project;
  private final AutoFetchExclusions exclusions;
  private AutoFetchComponent autoFetchComponent;
  private MessageBusConnection connection;

  AutoFetchSubscriber(@NotNull Project project) {
    this.project = project;
    exclusions = new AutoFetchExclusions(project);
  }

  @Override
  public void initComponent() {
    autoFetchComponent = Preconditions.checkNotNull(project.getComponent(AutoFetchComponent.class));
    connectToMessageBus();
  }

  private void connectToMessageBus() {
    connection = project.getMessageBus().connect();
    connection.subscribe(ConfigNotifier.CONFIG_TOPIC, new ConfigNotifier() {
      @Override
      public void configChanged(Project project, GitToolBoxConfigPrj previous, GitToolBoxConfigPrj current) {
        autoFetchComponent.configChanged(previous, current);
      }
    });
    connection.subscribe(AutoFetchNotifier.TOPIC, state -> autoFetchComponent.stateChanged(state));
    connection.subscribe(PerRepoInfoCache.CACHE_CHANGE, new PerRepoStatusCacheListener() {
      @Override
      public void stateChanged(@NotNull RepoInfo previous, @NotNull RepoInfo current,
                               @NotNull GitRepository repository) {
        handleAutoFetchOnBranchSwitch(previous, current, repository);
      }

      @Override
      public void evicted(@NotNull Collection<GitRepository> repositories) {
        AutoFetchOnBranchSwitch.getExistingInstance(project)
            .ifPresent(service -> service.onRepositoriesRemoved(repositories));
      }
    });
  }

  private void handleAutoFetchOnBranchSwitch(@NotNull RepoInfo previous, @NotNull RepoInfo current,
                                             @NotNull GitRepository repository) {
    if (GitToolBoxConfigPrj.getInstance(project).getAutoFetchOnBranchSwitch()) {
      if (!previous.isEmpty() && !current.isEmpty() && !previous.status().sameLocalBranch(current.status())) {
        if (exclusions.isAllowed(repository)) {
          //TODO: if branch makes through exclusions & inclusions
          AutoFetchOnBranchSwitch.getInstance(project).onBranchSwitch(current, repository);
        }
      } else {
        log.info("Branch switch not eligible for auto-fetch: previous=" + previous + ", current=" + current);
      }
    }
  }

  @Override
  public void disposeComponent() {
    if (connection != null) {
      connection.disconnect();
      connection = null;
    }
    autoFetchComponent = null;
  }
}
