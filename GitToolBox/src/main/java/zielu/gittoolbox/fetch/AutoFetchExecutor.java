package zielu.gittoolbox.fetch;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.ControlFlowException;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.GitToolBoxApp;
import zielu.gittoolbox.config.GitToolBoxConfigForProject;
import zielu.gittoolbox.metrics.Metrics;
import zielu.gittoolbox.metrics.MetricsHost;
import zielu.gittoolbox.util.ConcurrentUtil;

public class AutoFetchExecutor implements ProjectComponent {
  private final Logger log = Logger.getInstance(getClass());

  private final AtomicBoolean active = new AtomicBoolean();
  private final AtomicBoolean autoFetchEnabled = new AtomicBoolean();
  private final AtomicLong lastAutoFetchTimestamp = new AtomicLong();

  private final List<ScheduledFuture<?>> scheduledTasks = new LinkedList<>();
  private final AtomicInteger scheduledTasksCount = new AtomicInteger();
  private final Project project;
  private ScheduledExecutorService executor;
  private ExecutorService autoFetchRepoExecutor;

  public AutoFetchExecutor(@NotNull Project project) {
    this.project = project;
    Metrics metrics = MetricsHost.project(project);
    metrics.gauge("auto-fetch-tasks-size", scheduledTasksCount::get);
    metrics.gauge("auto-fetch-last-timestamp", lastAutoFetchTimestamp::get);
  }

  @Override
  public void initComponent() {
    executor = GitToolBoxApp.getInstance().autoFetchExecutor();
    autoFetchRepoExecutor = createExecutorService();
  }

  private ExecutorService createExecutorService() {
    return Executors.newFixedThreadPool(4, createThreadFactory());
  }

  private ThreadFactory createThreadFactory() {
    return new ThreadFactoryBuilder().setDaemon(true).setNameFormat("AutoFetchForRepo-%s").build();
  }

  @Override
  public void disposeComponent() {
    ConcurrentUtil.shutdown(autoFetchRepoExecutor);
    autoFetchRepoExecutor = null;
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

  private int getIntervalMinutes() {
    return GitToolBoxConfigForProject.getInstance(project).autoFetchIntervalMinutes;
  }

  synchronized void scheduleNextTask() {
    trySchedulingNextTask();
  }

  private void trySchedulingNextTask() {
    if (active.get() && autoFetchEnabled.get()) {
      scheduleTask(getIntervalMinutes());
    }
  }

  void scheduleTask(int delayMinutes) {
    if (active.get() && autoFetchEnabled.get()) {
      trySchedulingTask(delayMinutes);
    }
  }

  void rescheduleTask(int delayMinutes) {
    cancelCurrentTasks();
    if (active.get() && autoFetchEnabled.get()) {
      trySchedulingTask(delayMinutes);
    }
  }

  void scheduleInitTask() {
    scheduleFastTask(30);
  }

  void rescheduleFastTask() {
    cancelCurrentTasks();
    scheduleFastTask(45);
  }

  private void scheduleFastTask(int seconds) {
    if (active.get()) {
      trySchedulingFastTask(seconds);
    }
  }

  private synchronized void trySchedulingFastTask(int seconds) {
    if (cleanAndCheckTasks()) {
      submitFastTask(seconds);
    } else {
      log.debug("Tasks already scheduled (in fast auto-fetch)");
    }
  }

  private void submitFastTask(int seconds) {
    log.debug("Scheduling fast auto-fetch in ", seconds, " seconds");
    submitTaskToExecutor(seconds, TimeUnit.SECONDS);
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

  private synchronized void trySchedulingTask(int delayMinutes) {
    if (cleanAndCheckTasks()) {
      submitTask(delayMinutes);
    } else {
      log.debug("Tasks already scheduled (in regular auto-fetch)");
    }
  }

  private void submitTask(int delayMinutes) {
    log.debug("Scheduling regular auto-fetch in ", delayMinutes, "  minutes");
    submitTaskToExecutor(delayMinutes, TimeUnit.MINUTES);
  }

  private void submitTaskToExecutor(int delay, TimeUnit timeUnit) {
    scheduledTasks.add(executor.schedule(new AutoFetchTask(project, this), delay, timeUnit));
    scheduledTasksCount.set(scheduledTasks.size());
  }

  boolean runIfActive(Runnable task) {
    if (active.get()) {
      task.run();
      return true;
    }
    return false;
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

  void updateLastAutoFetchDate() {
    lastAutoFetchTimestamp.set(System.currentTimeMillis());
  }

  long getLastAutoFetchDate() {
    return lastAutoFetchTimestamp.get();
  }

  Executor repoFetchExecutor() {
    return autoFetchRepoExecutor;
  }
}
