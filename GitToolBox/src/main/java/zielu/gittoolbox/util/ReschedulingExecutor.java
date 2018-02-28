package zielu.gittoolbox.util;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReschedulingExecutor implements Disposable {
  private final Logger log = Logger.getInstance(getClass());
  private final ConcurrentMap<String, Future<?>> tasks = new ConcurrentHashMap<>();
  private final AtomicBoolean active = new AtomicBoolean(true);
  private final ScheduledExecutorService executor;
  private final boolean mayInterrupt;

  public ReschedulingExecutor(ScheduledExecutorService executor, boolean mayInterrupt) {
    this.executor = executor;
    this.mayInterrupt = mayInterrupt;
  }

  public Future<?> schedule(String id, Runnable task, long delay, TimeUnit timeUnit) {
    if (active.get()) {
      ScheduledFuture<?> newFuture = executor.schedule(task, delay, timeUnit);
      log.debug("Scheduled ", id, ": ", task);
      Optional.ofNullable(tasks.put(id, newFuture)).ifPresent(oldFuture -> {
        log.debug("Cancelling ", id, " interrupt=", mayInterrupt, ": ", oldFuture);
        oldFuture.cancel(mayInterrupt);
        log.debug("Cancelled ", id, " interrupt=", mayInterrupt, ": ", oldFuture);
      });
      return newFuture;
    } else {
      log.debug("Schedule ", id, " while inactive: ", task);
      return Futures.immediateCancelledFuture();
    }
  }

  @Override
  public void dispose() {
    if (active.compareAndSet(true, false)) {
      ImmutableList<Future<?>> existingTasks = ImmutableList.copyOf(tasks.values());
      tasks.clear();
      existingTasks.forEach(task -> task.cancel(mayInterrupt));
    }
  }
}
