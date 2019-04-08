package zielu.gittoolbox;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.intellij.openapi.diagnostic.Logger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import zielu.gittoolbox.util.AppUtil;

public class GitToolBoxApp {
  private final Logger log = Logger.getInstance(getClass());
  private final ScheduledExecutorService autoFetchExecutor;
  private final ScheduledExecutorService tasksExecutor;

  GitToolBoxApp() {
    autoFetchExecutor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors(),
        new ThreadFactoryBuilder().setDaemon(true).setNameFormat("AutoFetch-%s").build()
    );
    log.debug("Created auto-fetch executor: ", autoFetchExecutor);
    tasksExecutor = Executors.newScheduledThreadPool(2,
        new ThreadFactoryBuilder().setDaemon(true).setNameFormat("GtTask-%s").build()
    );
    log.debug("Created tasks executor: ", tasksExecutor);
  }

  public static GitToolBoxApp getInstance() {
    return AppUtil.getServiceInstance(GitToolBoxApp.class);
  }

  public ScheduledExecutorService autoFetchExecutor() {
    return autoFetchExecutor;
  }

  public ScheduledExecutorService tasksExecutor() {
    return tasksExecutor;
  }
}
