package zielu.gittoolbox.fetch;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.ControlFlowException;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import git4idea.repo.GitRepository;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.GitToolBoxApp;
import zielu.gittoolbox.metrics.ProjectMetrics;

public class AutoFetchExecutor implements ProjectComponent {
  private final Logger log = Logger.getInstance(getClass());

  private final AtomicBoolean active = new AtomicBoolean();
  private final AtomicBoolean autoFetchEnabled = new AtomicBoolean();

  private final List<ScheduledFuture<?>> scheduledTasks = new LinkedList<>();
  private final AtomicInteger scheduledTasksCount = new AtomicInteger();
  private final Project project;
  private final AutoFetchSchedule schedule;
  private ScheduledExecutorService executor;

  public AutoFetchExecutor(@NotNull Project project, @NotNull AutoFetchSchedule schedule,
                           @NotNull ProjectMetrics metrics) {
    this.project = project;
    this.schedule = schedule;
    metrics.gauge("auto-fetch-tasks-size", scheduledTasksCount::get);
  }

  @Override
  public void initComponent() {
    executor = GitToolBoxApp.getInstance().autoFetchExecutor();
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

  void scheduleTask(Duration delay) {
    if (active.get() && autoFetchEnabled.get()) {
      trySchedulingTask(delay);
    }
  }

  void scheduleTask(Duration delay, @NotNull GitRepository repository) {
    if (active.get() && autoFetchEnabled.get()) {
      trySchedulingTask(delay, new AutoFetchTask(project, this, schedule, repository));
    }
  }

  void rescheduleTask(Duration delay) {
    cancelCurrentTasks();
    if (active.get() && autoFetchEnabled.get()) {
      trySchedulingTask(delay);
    }
  }

  private synchronized void cancelCurrentTasks() {
    scheduledTasks.forEach(t -> t.cancel(true));
    scheduledTasks.clear();
    scheduledTasksCount.set(scheduledTasks.size());
  }

  private synchronized boolean cleanAndCheckTasks() {
    scheduledTasks.removeIf(task -> task.isCancelled() || task.isDone());
    scheduledTasksCount.set(scheduledTasks.size());
    return scheduledTasks.isEmpty();
  }

  private synchronized void trySchedulingTask(Duration delay) {
    trySchedulingTask(delay, new AutoFetchTask(project, this, schedule));
  }

  private synchronized void trySchedulingTask(Duration delay, Runnable task) {
    if (cleanAndCheckTasks()) {
      submitTask(delay, task);
    } else {
      log.debug("Tasks already scheduled (in regular auto-fetch)");
    }
  }

  private void submitTask(Duration delay, Runnable task) {
    log.debug("Scheduling auto-fetch in ", delay);
    submitTaskToExecutor(delay, task);
  }

  private void submitTaskToExecutor(Duration delay, Runnable task) {
    scheduledTasks.add(executor.schedule(task, delay.toMillis(), TimeUnit.MILLISECONDS));
    scheduledTasksCount.set(scheduledTasks.size());
  }

  void runIfActive(Runnable task) {
    if (active.get()) {
      task.run();
    }
  }

  <T> Optional<T> callIfActive(Callable<T> task) {
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
}
