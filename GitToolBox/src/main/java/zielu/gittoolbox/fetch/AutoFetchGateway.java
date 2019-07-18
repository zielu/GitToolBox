package zielu.gittoolbox.fetch;

import com.intellij.openapi.project.Project;
import com.intellij.util.concurrency.AppExecutorUtil;
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.util.GatewayBase;
import zielu.gittoolbox.util.MemoizeSupplier;

class AutoFetchGateway extends GatewayBase {
  private final Clock clock = Clock.systemDefaultZone();
  private final Supplier<ScheduledExecutorService> autoFetchExecutor;

  AutoFetchGateway(@NotNull Project project) {
    super(project);
    autoFetchExecutor = new MemoizeSupplier<>(() ->
      AppExecutorUtil.createBoundedScheduledExecutorService("GtAutoFetch", 1));
  }

  @NotNull
  Clock getClock() {
    return clock;
  }

  @NotNull
  Project project() {
    return project;
  }

  @NotNull
  ScheduledFuture schedule(@NotNull Runnable task, @NotNull Duration delay) {
    return autoFetchExecutor.get().schedule(task, delay.toMillis(), TimeUnit.MILLISECONDS);
  }
}
