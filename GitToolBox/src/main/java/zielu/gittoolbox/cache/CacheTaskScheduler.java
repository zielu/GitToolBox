package zielu.gittoolbox.cache;

import com.codahale.metrics.Counter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import git4idea.repo.GitRepository;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.ProjectAware;
import zielu.gittoolbox.metrics.Metrics;

class CacheTaskScheduler implements ProjectAware {
  private static final long TASK_DELAY_MILLIS = 170;

  private final Logger log = Logger.getInstance(getClass());
  private final AtomicBoolean active = new AtomicBoolean();
  private final Project project;
  private final Multimap<GitRepository, CacheTask> scheduledRepositories = HashMultimap.create();
  private final Counter statusQueueSize;
  private final Counter discardedTasksCount;
  private ScheduledExecutorService updateExecutor;

  CacheTaskScheduler(@NotNull Project project, @NotNull Metrics metrics) {
    this.project = project;
    statusQueueSize = metrics.counter("info-cache-queue-size");
    discardedTasksCount = metrics.counter("info-cache-discarded-updates");
  }

  void initialize() {
    updateExecutor = createExecutor();
  }

  private ScheduledExecutorService createExecutor() {
    ThreadFactoryBuilder threadBuilder = new ThreadFactoryBuilder()
        .setDaemon(true)
        .setNameFormat(getClass().getSimpleName() + "-[" + project.getName() + "]-%d");
    return Executors.newSingleThreadScheduledExecutor(threadBuilder.build());
  }

  void dispose() {
    destroyExecutor();
  }

  private void destroyExecutor() {
    List<Runnable> notStartedTasks = updateExecutor.shutdownNow();
    statusQueueSize.dec(notStartedTasks.size());
    notStartedTasks.forEach(notStarted -> log.info("Task " + notStarted + " was never started"));
  }

  @Override
  public void opened() {
    active.compareAndSet(false, true);
  }

  @Override
  public void closed() {
    active.compareAndSet(true, false);
  }

  void scheduleOptional(@NotNull GitRepository repository, @NotNull Task task) {
    if (active.get()) {
      scheduleInternal(repository, task, false);
    } else {
      log.debug("Inactive - ignored scheduling optional ", task, " for ", repository);
    }
  }

  void scheduleMandatory(@NotNull GitRepository repository, @NotNull Task task) {
    if (active.get()) {
      scheduleInternal(repository, task, true);
    } else {
      log.debug("Inactive - ignored scheduling mandatory ", task, " for ", repository);
    }
  }

  private synchronized void scheduleInternal(@NotNull GitRepository repository, @NotNull Task task,
                                             boolean mandatory) {
    CacheTask taskToSubmit = null;
    if (mandatory) {
      taskToSubmit = new CacheTask(repository, task);
      scheduledRepositories.put(repository, taskToSubmit);
    } else {
      Collection<CacheTask> alreadyScheduled = scheduledRepositories.get(repository);
      if (alreadyScheduled.isEmpty()) {
        taskToSubmit = new CacheTask(repository, task);
        scheduledRepositories.put(repository, taskToSubmit);
      } else {
        log.debug("Tasks for ", repository, " already scheduled: ", alreadyScheduled);
      }
    }

    if (taskToSubmit != null) {
      submitForExecution(taskToSubmit);
    } else {
      discardedTasksCount.inc();
    }
  }

  synchronized void removeRepository(@NotNull GitRepository repository) {
    scheduledRepositories.removeAll(repository).forEach(CacheTask::kill);
  }

  private void submitForExecution(CacheTask task) {
    updateExecutor.schedule(task, TASK_DELAY_MILLIS, TimeUnit.MILLISECONDS);
    statusQueueSize.inc();
    log.debug("Scheduled: ", task);
  }

  private class CacheTask implements Runnable {
    private final AtomicBoolean taskActive = new AtomicBoolean(true);
    private final Task task;
    final GitRepository repository;

    CacheTask(@NotNull GitRepository repository, @NotNull Task task) {
      this.repository = repository;
      this.task = task;
    }

    void kill() {
      taskActive.set(false);
    }

    @Override
    public void run() {
      synchronized (CacheTaskScheduler.this) {
        if (scheduledRepositories.remove(repository, this)) {
          statusQueueSize.dec();
        }
      }
      if (active.get() && taskActive.get()) {
        task.run(repository);
      }
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
          .append("repository", repository)
          .append("task", task)
          .build();
    }
  }

  interface Task {
    void run(@NotNull GitRepository repository);
  }
}
