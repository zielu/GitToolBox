package zielu.gittoolbox.fetch;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.concurrency.AppExecutorUtil;
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.util.AppUtil;
import zielu.gittoolbox.util.GatewayBase;
import zielu.gittoolbox.util.MemoizeSupplier;

class AutoFetchGateway extends GatewayBase {
  private final Logger log = Logger.getInstance(getClass());
  private final Clock clock = Clock.systemDefaultZone();
  private final Supplier<ScheduledExecutorService> autoFetchExecutor;

  AutoFetchGateway(@NotNull Project project) {
    super(project);
    autoFetchExecutor = new MemoizeSupplier<>(() ->
      AppExecutorUtil.createBoundedScheduledExecutorService("GtAutoFetch", 1));
  }

  @NotNull
  static AutoFetchGateway getInstance(@NotNull Project project) {
    return AppUtil.getServiceInstance(project, AutoFetchGateway.class);
  }

  @NotNull
  Clock getClock() {
    return clock;
  }

  @NotNull
  ScheduledFuture<?> scheduleAutoFetch(@NotNull Duration delay,
                                       @NotNull BiFunction<Project, AutoFetchSchedule, Runnable> taskCreator) {
    Runnable task = taskCreator.apply(project, AutoFetchSchedule.getInstance(project));
    log.debug("Scheduling auto-fetch in ", delay);
    return schedule(delay, task);
  }

  @NotNull
  private ScheduledFuture<?> schedule(@NotNull Duration delay, @NotNull Runnable task) {
    return autoFetchExecutor.get().schedule(task, delay.toMillis(), TimeUnit.MILLISECONDS);
  }
}
