package zielu.gittoolbox.fetch;

import com.google.common.collect.Lists;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.GitToolBoxApp;
import zielu.gittoolbox.config.ConfigNotifier;
import zielu.gittoolbox.config.GitToolBoxConfigForProject;

public class AutoFetch implements ProjectComponent {
  private static final int DEFAULT_DELAY_MINUTES = 1;
  private final Logger log = Logger.getInstance(getClass());

  private final AtomicLong lastAutoFetchTimestamp = new AtomicLong();
  private final AtomicBoolean active = new AtomicBoolean();
  private final List<ScheduledFuture<?>> scheduledTasks = new LinkedList<>();
  private final Project project;
  private MessageBusConnection connection;
  private ScheduledExecutorService executor;
  private int currentInterval;

  public AutoFetch(@NotNull Project project) {
    this.project = project;
  }

  @SuppressFBWarnings({"NP_NULL_ON_SOME_PATH"})
  @NotNull
  public static AutoFetch getInstance(@NotNull Project project) {
    return project.getComponent(AutoFetch.class);
  }

  public static AutoFetch create(Project project) {
    return new AutoFetch(project);
  }

  @Override
  public void initComponent() {
    connectToMessageBus();
  }

  private void connectToMessageBus() {
    connection = project.getMessageBus().connect();
    connection.subscribe(ConfigNotifier.CONFIG_TOPIC, new ConfigNotifier.Adapter() {
      @Override
      public void configChanged(Project project, GitToolBoxConfigForProject config) {
        onConfigChange(config);
      }
    });
    connection.subscribe(AutoFetchNotifier.TOPIC, this::onStateChanged);
  }

  private void initializeFirstTask() {
    GitToolBoxConfigForProject config = GitToolBoxConfigForProject.getInstance(project());
    if (config.autoFetch) {
      scheduleFirstTask(config);
    }
  }

  private void scheduleFirstTask(GitToolBoxConfigForProject config) {
    synchronized (this) {
      currentInterval = config.autoFetchIntervalMinutes;
      scheduleInitTask();
    }
  }

  private synchronized void cancelCurrentTasks() {
    List<ScheduledFuture<?>> tasks = Lists.newArrayList(scheduledTasks);
    scheduledTasks.clear();
    tasks.forEach(t -> t.cancel(true));
  }

  private synchronized boolean cleanAndCheckTasks() {
    scheduledTasks.removeIf(task -> task.isCancelled() || task.isDone());
    return scheduledTasks.isEmpty();
  }

  private void onConfigChange(@NotNull GitToolBoxConfigForProject config) {
    if (config.autoFetch) {
      log.debug("Auto-fetch enabled");
      autoFetchEnabled(config);
    } else {
      log.debug("Auto-fetch disabled");
      autoFetchDisabled();
    }
  }

  private synchronized void autoFetchEnabled(@NotNull GitToolBoxConfigForProject config) {
    if (currentInterval != config.autoFetchIntervalMinutes) {
      autoFetchIntervalChanged(config);
    } else {
      log.debug("Auto-fetch interval and state did not change: enabled=", config.autoFetch,
          ", interval=", config.autoFetchIntervalMinutes);
    }
  }

  private void autoFetchIntervalChanged(@NotNull GitToolBoxConfigForProject config) {
    log.debug("Auto-fetch interval or state changed: enabled=", config.autoFetch,
        ", interval=", config.autoFetchIntervalMinutes);
    cancelCurrentTasks();
    log.debug("Existing task cancelled on auto-fetch change");
    if (currentInterval == 0) {
      scheduleFastTask();
    } else {
      scheduleTask();
    }
    currentInterval = config.autoFetchIntervalMinutes;
  }

  private synchronized void autoFetchDisabled() {
    cancelCurrentTasks();
    currentInterval = 0;
    log.debug("Existing task cancelled on auto-fetch disable");
  }

  private void scheduleInitTask() {
    scheduleFastTask(30);
  }

  private void scheduleFastTask() {
    scheduleFastTask(60);
  }

  private void scheduleFastTask(int seconds) {
    if (isActive()) {
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

  private void onStateChanged(@NotNull AutoFetchState state) {
    if (isAutoFetchEnabled(state)) {
      scheduleTaskOnStateChange();
    }
  }

  private boolean isAutoFetchEnabled(@NotNull AutoFetchState state) {
    return state.canAutoFetch() && isAutoFetchEnabled();
  }

  private boolean isAutoFetchEnabled() {
    return GitToolBoxConfigForProject.getInstance(project()).autoFetch;
  }

  private void scheduleTaskOnStateChange() {
    int delayMinutes = calculateTaskDelayMinutesOnStateChange();
    scheduleTask(delayMinutes);
  }

  private int calculateTaskDelayMinutesOnStateChange() {
    int delayMinutes;
    long lastAutoFetch = lastAutoFetch();
    if (lastAutoFetch != 0) {
      delayMinutes = calculateDelayMinutesIfTaskWasExecuted(lastAutoFetch);
    } else {
      delayMinutes = DEFAULT_DELAY_MINUTES;
    }
    return delayMinutes;
  }

  private int calculateDelayMinutesIfTaskWasExecuted(long lastAutoFetch) {
    long nextAutoFetch = lastAutoFetch + TimeUnit.MINUTES.toMillis(getIntervalMinutes());
    long difference = nextAutoFetch - System.currentTimeMillis();
    if (difference > 0) {
      return Math.max((int) TimeUnit.MILLISECONDS.toMinutes(difference), DEFAULT_DELAY_MINUTES);
    } else {
      return DEFAULT_DELAY_MINUTES;
    }
  }

  private int getIntervalMinutes() {
    return GitToolBoxConfigForProject.getInstance(project()).autoFetchIntervalMinutes;
  }

  private void scheduleTask() {
    scheduleTask(getIntervalMinutes());
  }

  private void scheduleTask(int delayMinutes) {
    if (isActive()) {
      trySchedulingTask(delayMinutes);
    }
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
    scheduledTasks.add(executor.schedule(AutoFetchTask.create(this), delay, timeUnit));
  }

  void scheduleNextTask() {
    synchronized (this) {
      trySchedulingNextTask();
    }
  }

  private void trySchedulingNextTask() {
    if (isActive() && isAutoFetchEnabled()) {
      scheduleTask();
    }
  }

  public Project project() {
    return project;
  }

  private boolean isActive() {
    return active.get();
  }

  public void updateLastAutoFetchDate() {
    lastAutoFetchTimestamp.set(System.currentTimeMillis());
  }

  public long lastAutoFetch() {
    return lastAutoFetchTimestamp.get();
  }

  @Override
  public void projectOpened() {
    if (active.compareAndSet(false, true)) {
      initializeExecutor();
      initializeFirstTask();
    }
  }

  private synchronized void initializeExecutor() {
    executor = GitToolBoxApp.getInstance().autoFetchExecutor();
  }

  @Override
  public void projectClosed() {
    if (active.compareAndSet(true, false)) {
      cancelCurrentTasks();
    }
  }

  @Override
  public void disposeComponent() {
    if (connection != null) {
      connection.disconnect();
      connection = null;
    }
  }

  void runIfActive(Runnable task) {
    if (isActive()) {
      task.run();
    }
  }

  <T> Optional<T> callIfActive(Callable<T> task) {
    if (isActive()) {
      try {
        return Optional.of(task.call());
      } catch (Exception e) {
        log.error("Error while calling if active", e);
        return Optional.empty();
      }
    } else {
      return Optional.empty();
    }
  }
}
