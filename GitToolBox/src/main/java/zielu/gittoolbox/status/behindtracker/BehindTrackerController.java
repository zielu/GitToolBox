package zielu.gittoolbox.status.behindtracker;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.cache.PerRepoInfoCache;
import zielu.gittoolbox.cache.PerRepoStatusCacheListener;
import zielu.gittoolbox.cache.RepoInfo;
import zielu.gittoolbox.util.GtUtil;

public class BehindTrackerController implements ProjectComponent {
  private final Logger log = Logger.getInstance(getClass());
  private final Project project;
  private BehindTracker behindTracker;
  private MessageBusConnection connection;

  public BehindTrackerController(@NotNull Project project) {
    this.project = project;
  }

  @Override
  public void initComponent() {
    behindTracker = BehindTracker.getInstance(project);
    connectToMessageBus();
  }

  private void connectToMessageBus() {
    connection = project.getMessageBus().connect();
    connection.subscribe(PerRepoInfoCache.CACHE_CHANGE, new PerRepoStatusCacheListener() {
      @Override
      public void stateChanged(@NotNull RepoInfo info,
                               @NotNull GitRepository repository) {
        if (log.isDebugEnabled()) {
          log.debug("State changed [", GtUtil.name(repository), "]: ", info);
        }
        behindTracker.onStateChange(repository, info);
      }
    });
  }

  @Override
  public void projectClosed() {
    disconnectFromMessageBus();
  }

  private void disconnectFromMessageBus() {
    connection.disconnect();
  }

  @Override
  public void disposeComponent() {
    connection = null;
  }
}
