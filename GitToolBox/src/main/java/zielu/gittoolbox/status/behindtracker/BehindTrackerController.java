package zielu.gittoolbox.status.behindtracker;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import git4idea.repo.GitRepository;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.GitToolBoxApp;
import zielu.gittoolbox.cache.PerRepoInfoCache;
import zielu.gittoolbox.cache.PerRepoStatusCacheListener;
import zielu.gittoolbox.cache.RepoInfo;
import zielu.gittoolbox.util.DisposeSafeRunnable;
import zielu.gittoolbox.util.GtUtil;
import zielu.gittoolbox.util.ReschedulingExecutor;

public class BehindTrackerController implements ProjectComponent {
  private final Logger log = Logger.getInstance(getClass());
  private final AtomicBoolean active = new AtomicBoolean();
  private final Project project;
  private ReschedulingExecutor executor;
  private BehindTracker behindTracker;
  private MessageBusConnection connection;

  public BehindTrackerController(@NotNull Project project) {
    this.project = project;
  }

  @Override
  public void initComponent() {
    behindTracker = BehindTracker.getInstance(project);
    executor = new ReschedulingExecutor(GitToolBoxApp.getInstance().tasksExecutor(), true);
    connectToMessageBus();
  }

  @Override
  public void projectOpened() {
    active.compareAndSet(false, true);
  }

  private void connectToMessageBus() {
    connection = project.getMessageBus().connect();
    connection.subscribe(PerRepoInfoCache.CACHE_CHANGE, new PerRepoStatusCacheListener() {
      @Override
      public void stateChanged(@NotNull RepoInfo info,
                               @NotNull GitRepository repository) {
        if (active.get()) {
          handleStateChange(info, repository);
        }
      }
    });
  }

  private void handleStateChange(@NotNull RepoInfo info,
                                 @NotNull GitRepository repository) {
    if (log.isDebugEnabled()) {
      log.debug("State changed [", GtUtil.name(repository), "]: ", info);
    }
    behindTracker.onStateChange(repository, info);
    scheduleNotifyTask();
  }

  private void scheduleNotifyTask() {
    if (active.get()) {
      BehindNotifyTask task = new BehindNotifyTask(project);
      executor.schedule("behind-notify", new DisposeSafeRunnable(project, task), 10, TimeUnit.SECONDS);
    }
  }

  @Override
  public void projectClosed() {
    if (active.compareAndSet(true, false)) {
      disconnectFromMessageBus();
      executor.close();
    }
  }

  private void disconnectFromMessageBus() {
    if (connection != null) {
      connection.disconnect();
    }
  }

  @Override
  public void disposeComponent() {
    connection = null;
    if (executor != null) {
      executor.dispose();
      executor = null;
    }
  }
}
