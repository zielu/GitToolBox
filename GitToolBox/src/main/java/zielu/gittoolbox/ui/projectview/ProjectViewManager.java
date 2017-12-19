package zielu.gittoolbox.ui.projectview;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import git4idea.repo.GitRepository;
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
  private final AtomicBoolean opened = new AtomicBoolean();
  private final Project project;
  private final MessageBusConnection connection;

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
      public void stateChanged(@NotNull final RepoInfo info,
                               @NotNull final GitRepository repository) {
        refreshProjectView();
      }
    });
  }

  public static ProjectViewManager create(Project project) {
    return new ProjectViewManager(project);
  }

  private void refreshProjectView() {
    if (opened.get()) {
      AppUtil.invokeLaterIfNeeded(() -> {
        if (opened.get()) {
          ProjectView.getInstance(project).refresh();
        }
      });
    }
  }

  @Override
  public void opened() {
    opened.compareAndSet(false, true);
  }

  @Override
  public void closed() {
    opened.compareAndSet(true, false);
  }

  @Override
  public void dispose() {
    connection.disconnect();
  }
}
