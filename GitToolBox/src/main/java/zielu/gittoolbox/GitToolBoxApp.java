package zielu.gittoolbox;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.BaseComponent;
import com.intellij.openapi.diagnostic.Logger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import zielu.gittoolbox.util.ConcurrentUtil;

public class GitToolBoxApp implements BaseComponent {
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
    autoFetchExecutor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors(),
        new ThreadFactoryBuilder().setDaemon(true).setNameFormat("AutoFetch-%s").build()
    );
    log.debug("Created auto-fetch executor: ", autoFetchExecutor);
    tasksExecutor = Executors.newScheduledThreadPool(2,
        new ThreadFactoryBuilder().setDaemon(true).setNameFormat("GtTask-%s").build()
    );
    log.debug("Created tasks executor: ", tasksExecutor);
  }

  @Override
  public void disposeComponent() {
    ConcurrentUtil.shutdown(autoFetchExecutor);
    ConcurrentUtil.shutdown(tasksExecutor);
    autoFetchExecutor = null;
    tasksExecutor = null;
  }
}
