package zielu.gittoolbox.fetch;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.ControlFlowException;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.concurrency.AppExecutorUtil;
import git4idea.repo.GitRepository;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.metrics.ProjectMetrics;

public class AutoFetchExecutor implements ProjectComponent {
  private final Logger log = Logger.getInstance(getClass());

  private final AtomicBoolean active = new AtomicBoolean();
  private final AtomicBoolean autoFetchEnabled = new AtomicBoolean();

  private final List<ScheduledFuture<?>> scheduledCyclicTasks = new LinkedList<>();
  private final List<ScheduledFuture<?>> scheduledRepoTasks = new LinkedList<>();
  private final AtomicInteger scheduledCyclicTasksCount = new AtomicInteger();
  private final AtomicInteger scheduledRepoTasksCount = new AtomicInteger();
  private final Project project;
  private final AutoFetchSchedule schedule;
  private final Semaphore autoFetchLock = new Semaphore(1);
  private ScheduledExecutorService executor;

  public AutoFetchExecutor(@NotNull Project project, @NotNull AutoFetchSchedule schedule,
                           @NotNull ProjectMetrics metrics) {
    this.project = project;
    this.schedule = schedule;
    metrics.gauge("auto-fetch.cyclic-tasks-size", scheduledCyclicTasksCount::get);
    metrics.gauge("auto-fetch.repo-tasks-size", scheduledRepoTasksCount::get);
  }

  @Override
  public void initComponent() {
    executor = AppExecutorUtil.createBoundedScheduledExecutorService("GtAutoFetch", 1);
  }

  @Override
  public void projectOpened() {
    active.set(true);
  }

  @Override
  public void projectClosed() {
    if (active.compareAndSet(true, false)) {
      cancelCurrentTasks();
    }
  }

  void setAutoFetchEnabled(boolean enabled) {
    boolean wasEnabled = autoFetchEnabled.getAndSet(enabled);
    if (wasEnabled && !enabled) {
      cancelCurrentTasks();
    }
  }

  synchronized void scheduleNextTask() {
    trySchedulingNextTask();
  }

  private void trySchedulingNextTask() {
    if (active.get() && autoFetchEnabled.get()) {
      scheduleTask(schedule.getInterval());
    }
  }

  void scheduleTask(@NotNull Duration delay) {
    if (active.get() && autoFetchEnabled.get()) {
      trySchedulingTask(delay);
    }
  }

  void scheduleTask(@NotNull Duration delay, @NotNull GitRepository repository) {
    if (active.get() && autoFetchEnabled.get()) {
      trySchedulingTask(delay, repository);
    }
  }

  void rescheduleTask(Duration delay) {
    cancelCurrentTasks();
    if (active.get() && autoFetchEnabled.get()) {
      trySchedulingTask(delay);
    }
  }

  private synchronized void cancelCurrentTasks() {
    scheduledCyclicTasks.forEach(t -> t.cancel(true));
    scheduledCyclicTasks.clear();
    scheduledCyclicTasksCount.set(scheduledCyclicTasks.size());
  }

  private synchronized boolean cleanAndCheckTasks(List<ScheduledFuture<?>> tasks, AtomicInteger tasksCount) {
    tasks.removeIf(task -> task.isCancelled() || task.isDone());
    tasksCount.set(tasks.size());
    return tasks.isEmpty();
  }

  private synchronized void trySchedulingTask(Duration delay) {
    if (cleanAndCheckTasks(scheduledCyclicTasks, scheduledCyclicTasksCount)) {
      ScheduledFuture<?> scheduled = submitTaskToExecutor(delay, new AutoFetchTask(project, this, schedule));
      scheduledCyclicTasks.add(scheduled);
      scheduledCyclicTasksCount.set(scheduledCyclicTasks.size());
    } else {
      log.debug("Tasks already scheduled (in regular auto-fetch)");
    }
  }

  private synchronized void trySchedulingTask(Duration delay, GitRepository repository) {
    if (cleanAndCheckTasks(scheduledRepoTasks, scheduledRepoTasksCount)) {
      ScheduledFuture<?> scheduled = submitTaskToExecutor(delay,
          new AutoFetchTask(project, this, schedule, repository));
      scheduledRepoTasks.add(scheduled);
      scheduledRepoTasksCount.set(scheduledRepoTasks.size());
    } else {
      log.debug("Tasks already scheduled (in repo auto-fetch)");
    }
  }

  private ScheduledFuture<?> submitTaskToExecutor(Duration delay, Runnable task) {
    log.debug("Scheduling auto-fetch in ", delay);
    return executor.schedule(task, delay.toMillis(), TimeUnit.MILLISECONDS);
  }

  void runIfActive(@NotNull Runnable task) {
    if (active.get()) {
      task.run();
    }
  }

  <T> Optional<T> callIfActive(@NotNull Callable<T> task) {
    if (active.get()) {
      try {
        return Optional.of(task.call());
      } catch (Exception e) {
        if (e instanceof ControlFlowException) {
          log.info("Exception while calling if active", e);
        } else {
          log.error("Error while calling if active", e);
        }
        return Optional.empty();
      }
    } else {
      return Optional.empty();
    }
  }

  void acquireAutoFetchLock() {
    autoFetchLock.acquireUninterruptibly();
  }

  void releaseAutoFetchLock() {
    autoFetchLock.release();
  }
}
