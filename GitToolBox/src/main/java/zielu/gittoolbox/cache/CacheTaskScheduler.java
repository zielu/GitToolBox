package zielu.gittoolbox.cache;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.serviceContainer.NonInjectable;
import com.intellij.util.concurrency.AppExecutorUtil;
import git4idea.repo.GitRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.util.MemoizeSupplier;

class CacheTaskScheduler implements Disposable {
  private static final long TASK_DELAY_MILLIS = 170;

  private final Logger log = Logger.getInstance(getClass());
  private final AtomicBoolean active = new AtomicBoolean(true);
  private final Map<GitRepository, Collection<CacheTask>> scheduledRepositories = new ConcurrentHashMap<>();
  private final CacheTaskSchedulerLocalGateway gateway;
  private Supplier<ScheduledExecutorService> updateExecutor;
  private long taskDelayMillis = TASK_DELAY_MILLIS;

  CacheTaskScheduler(@NotNull Project project) {
    this(new CacheTaskSchedulerLocalGatewayImpl(project));
  }

  @NonInjectable
  CacheTaskScheduler(@NotNull CacheTaskSchedulerLocalGateway gateway) {
    this.gateway = gateway;
    updateExecutor = new MemoizeSupplier<>(this::createExecutor);
    gateway.disposeWithProject(this);
  }

  private ScheduledExecutorService createExecutor() {
    return AppExecutorUtil.createBoundedScheduledExecutorService("GtCache", 1);
  }

  void setTaskDelayMillis(long delayMillis) {
    taskDelayMillis = delayMillis;
  }

  @Override
  public void dispose() {
    active.compareAndSet(true, false);
    Collection<Collection<CacheTask>> tasks = new ArrayList<>(scheduledRepositories.values());
    scheduledRepositories.clear();
    tasks.stream().flatMap(Collection::stream).forEach(CacheTask::kill);
  }

  void scheduleOptional(@NotNull GitRepository repository, @NotNull Task task) {
    if (active.get()) {
      CacheTask taskToSubmit = storeTask(new SingleCacheTask(repository, task));
      if (taskToSubmit != null) {
        submitForExecution(taskToSubmit);
      } else {
        gateway.discardedTasksCounterInc();
        task.discarded();
        log.debug("Tasks for ", repository, " already scheduled");
      }
    } else {
      task.discarded();
      log.debug("Inactive - ignored scheduling optional ", task, " for ", repository);
    }
  }

  void scheduleMandatory(@NotNull GitRepository repository, @NotNull Task task) {
    if (active.get()) {
      submitForExecution(storeTask(new CacheTask(repository, task)));
    } else {
      task.discarded();
      log.debug("Inactive - ignored scheduling mandatory ", task, " for ", repository);
    }
  }

  private CacheTask storeTask(@NotNull CacheTask task) {
    if (scheduledRepositories.computeIfAbsent(task.repository, repo -> ConcurrentHashMap.newKeySet()).add(task)) {
      return task;
    }
    return null;
  }

  void removeRepository(@NotNull GitRepository repository) {
    Optional.ofNullable(scheduledRepositories.remove(repository)).ifPresent(tasks -> tasks.forEach(CacheTask::kill));
  }

  private void submitForExecution(CacheTask task) {
    updateExecutor.get().schedule(task, taskDelayMillis, TimeUnit.MILLISECONDS);
    gateway.queueSizeCounterInc();
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
      if (scheduledRepositories.getOrDefault(repository, Collections.emptySet()).remove(this)) {
        gateway.queueSizeCounterDec();
      }
      if (taskActive.get() && active.get()) {
        task.run(repository);
      } else {
        task.notRun();
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

  private class SingleCacheTask extends CacheTask {
    SingleCacheTask(@NotNull GitRepository repository, @NotNull Task task) {
      super(repository, task);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(repository);
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof CacheTask) {
        CacheTask other = (CacheTask) obj;
        return Objects.equals(repository, other.repository);
      }
      return false;
    }
  }

  interface Task {
    void run(@NotNull GitRepository repository);

    default void discarded() {
    }

    default void notRun() {
    }
  }
}
