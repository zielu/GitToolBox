package zielu.gittoolbox.ui.projectview;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import git4idea.repo.GitRepository;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.ProjectAware;
import zielu.gittoolbox.cache.PerRepoInfoCache;
import zielu.gittoolbox.cache.PerRepoStatusCacheListener;
import zielu.gittoolbox.cache.RepoInfo;
import zielu.gittoolbox.config.ConfigNotifier;
import zielu.gittoolbox.config.GitToolBoxConfig;
import zielu.gittoolbox.ui.util.AppUtil;

public class ProjectViewManager implements Disposable, ProjectAware {
  private final AtomicBoolean active = new AtomicBoolean();
  private final Project project;
  private MessageBusConnection connection;

  private ProjectViewManager(Project project) {
    this.project = project;
    connection = this.project.getMessageBus().connect();
    connection.subscribe(ConfigNotifier.CONFIG_TOPIC, new ConfigNotifier.Adapter() {
      @Override
      public void configChanged(GitToolBoxConfig config) {
        refreshProjectView();
      }
    });
    connection.subscribe(PerRepoInfoCache.CACHE_CHANGE, new PerRepoStatusCacheListener() {
      @Override
      public void stateChanged(@NotNull final RepoInfo info, @NotNull final GitRepository repository) {
        refreshProjectView();
      }

      @Override
      public void evicted(@NotNull Collection<GitRepository> repositories) {
        refreshProjectView();
      }
    });
  }

  public static ProjectViewManager create(Project project) {
    return new ProjectViewManager(project);
  }

  private void refreshProjectView() {
    if (active.get()) {
      AppUtil.invokeLaterIfNeeded(() -> {
        if (active.get()) {
          ProjectView.getInstance(project).refresh();
        }
      });
    }
  }

  @Override
  public void opened() {
    active.compareAndSet(false, true);
  }

  @Override
  public void closed() {
    active.compareAndSet(true, false);
  }

  @Override
  public void dispose() {
    if (connection != null) {
      connection.disconnect();
      connection = null;
    }
  }
}
