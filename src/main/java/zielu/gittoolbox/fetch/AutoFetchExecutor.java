package zielu.gittoolbox.fetch;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import git4idea.repo.GitRepository;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.metrics.ProjectMetrics;
import zielu.gittoolbox.util.AppUtil;
import zielu.intellij.metrics.Metrics;
import zielu.intellij.util.ZDisposeGuard;

class AutoFetchExecutor implements Disposable {
  private final Logger log = Logger.getInstance(getClass());

  private final ZDisposeGuard disposeGuard = new ZDisposeGuard();
  private final AtomicBoolean autoFetchEnabled = new AtomicBoolean();

  private final List<ScheduledFuture<?>> scheduledCyclicTasks = new LinkedList<>();
  private final List<ScheduledFuture<?>> scheduledRepoTasks = new LinkedList<>();
  private final AtomicInteger scheduledCyclicTasksCount = new AtomicInteger();
  private final AtomicInteger scheduledRepoTasksCount = new AtomicInteger();
  private final Semaphore autoFetchLock = new Semaphore(1);

  private final Project project;

  AutoFetchExecutor(@NotNull Project project) {
    this.project = project;
    Metrics metrics = ProjectMetrics.getInstance(project);
    metrics.gauge("auto-fetch.cyclic-tasks.size", scheduledCyclicTasksCount::get);
    metrics.gauge("auto-fetch.repo-tasks.size", scheduledRepoTasksCount::get);
    Disposer.register(this, disposeGuard);
  }

  @NotNull
  static AutoFetchExecutor getInstance(@NotNull Project project) {
    return AppUtil.getServiceInstance(project, AutoFetchExecutor.class);
  }

  @Override
  public void dispose() {
    cancelCurrentTasks();
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
    if (disposeGuard.isActive()) {
      scheduleTask(AutoFetchSchedule.getInstance(project).getInterval());
    }
  }

  void scheduleTask(@NotNull Duration delay) {
    if (disposeGuard.isActive() && autoFetchEnabled.get()) {
      trySchedulingTask(delay);
    }
  }

  void scheduleTask(@NotNull Duration delay, @NotNull GitRepository repository) {
    if (disposeGuard.isActive() && autoFetchEnabled.get()) {
      trySchedulingTask(delay, repository);
    }
  }

  void rescheduleTask(Duration delay) {
    cancelCurrentTasks();
    if (disposeGuard.isActive() && autoFetchEnabled.get()) {
      trySchedulingTask(delay);
    }
  }

  private synchronized void cancelCurrentTasks() {
    scheduledCyclicTasks.forEach(t -> t.cancel(true));
    scheduledCyclicTasks.clear();
    scheduledCyclicTasksCount.set(0);
  }

  private synchronized boolean cleanAndCheckTasks(List<ScheduledFuture<?>> tasks, AtomicInteger tasksCount) {
    tasks.removeIf(task -> task.isCancelled() || task.isDone());
    tasksCount.set(tasks.size());
    return tasks.isEmpty();
  }

  private synchronized void trySchedulingTask(Duration delay) {
    if (cleanAndCheckTasks(scheduledCyclicTasks, scheduledCyclicTasksCount)) {
      AutoFetchFacade.getInstance(project)
          .scheduleAutoFetch(delay, (myProject, schedule) ->
              new AutoFetchTask(myProject, AutoFetchExecutor.this, schedule))
          .ifPresent(scheduled -> {
            scheduledCyclicTasks.add(scheduled);
            scheduledCyclicTasksCount.set(scheduledCyclicTasks.size());
          });
    } else {
      log.debug("Tasks already scheduled (in regular auto-fetch)");
    }
  }

  private synchronized void trySchedulingTask(Duration delay, GitRepository repository) {
    if (cleanAndCheckTasks(scheduledRepoTasks, scheduledRepoTasksCount)) {
      AutoFetchFacade.getInstance(project)
          .scheduleAutoFetch(delay, (myProject, schedule) ->
              new AutoFetchTask(myProject, AutoFetchExecutor.this, schedule, repository))
          .ifPresent(scheduled -> {
            scheduledRepoTasks.add(scheduled);
            scheduledRepoTasksCount.set(scheduledRepoTasks.size());
          });
    } else {
      log.debug("Tasks already scheduled (in repo auto-fetch)");
    }
  }

  void acquireAutoFetchLock() {
    autoFetchLock.acquireUninterruptibly();
  }

  void releaseAutoFetchLock() {
    autoFetchLock.release();
  }
}
