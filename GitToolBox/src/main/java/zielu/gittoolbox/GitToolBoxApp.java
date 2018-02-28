package zielu.gittoolbox;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class GitToolBoxApp implements ApplicationComponent {
  private final Logger log = Logger.getInstance(getClass());
  private ScheduledExecutorService autoFetchExecutor;
  private ScheduledExecutorService tasksExecutor;

  public static GitToolBoxApp getInstance() {
    return ApplicationManager.getApplication().getComponent(GitToolBoxApp.class);
  }

  public ScheduledExecutorService autoFetchExecutor() {
    return autoFetchExecutor;
  }

  public ScheduledExecutorService tasksExecutor() {
    return tasksExecutor;
  }

  @Override
  public void initComponent() {
    autoFetchExecutor = Executors.newSingleThreadScheduledExecutor(
        new ThreadFactoryBuilder().setDaemon(true).setNameFormat("AutoFetch-%s").build()
    );
    tasksExecutor = Executors.newScheduledThreadPool(2,
        new ThreadFactoryBuilder().setDaemon(true).setNameFormat("GtTask-%s").build()
    );
  }

  @Override
  public void disposeComponent() {
    shutdown(autoFetchExecutor);
    shutdown(tasksExecutor);
  }

  private void shutdown(ScheduledExecutorService executor) {
    executor.shutdownNow().forEach(notStarted -> log.info("Task " + notStarted + " was never started"));
  }
}
