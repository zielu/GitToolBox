package zielu.gittoolbox.fetch;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.config.GitToolBoxConfigForProject;

public class AutoFetch implements ProjectComponent, AutoFetchComponent {
  private static final int DEFAULT_DELAY_MINUTES = 1;
  private final Logger log = Logger.getInstance(getClass());

  private final AtomicBoolean active = new AtomicBoolean();
  private final AtomicBoolean autoFetchEnabled = new AtomicBoolean();
  private final Project project;
  private final AutoFetchExecutor scheduler;
  private int currentInterval;

  AutoFetch(@NotNull Project project, @NotNull AutoFetchExecutor scheduler) {
    this.project = project;
    this.scheduler = scheduler;
  }

  @Override
  public void initComponent() {
    updateAutoFetchEnabled(getConfig());
  }

  private GitToolBoxConfigForProject getConfig() {
    return GitToolBoxConfigForProject.getInstance(project());
  }

  private void updateAutoFetchEnabled(GitToolBoxConfigForProject config) {
    autoFetchEnabled.set(config.autoFetch);
    scheduler.setAutoFetchEnabled(autoFetchEnabled.get());
  }

  @NotNull
  public static AutoFetch getInstance(@NotNull Project project) {
    return project.getComponent(AutoFetch.class);
  }

  private void initializeFirstTask() {
    if (autoFetchEnabled.get()) {
      scheduleFirstTask(getConfig());
    }
  }

  private void scheduleFirstTask(GitToolBoxConfigForProject config) {
    currentInterval = config.autoFetchIntervalMinutes;
    scheduler.scheduleInitTask();
  }

  @Override
  public void configChanged(@NotNull GitToolBoxConfigForProject config) {
    updateAutoFetchEnabled(config);
    if (autoFetchEnabled.get()) {
      log.debug("Auto-fetch enabled");
      autoFetchEnabled(config);
    } else {
      log.debug("Auto-fetch disabled");
      autoFetchDisabled();
    }
  }

  private void autoFetchEnabled(@NotNull GitToolBoxConfigForProject config) {
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
    log.debug("Existing task cancelled on auto-fetch change");
    if (currentInterval == 0) {
      scheduler.rescheduleFastTask();
    } else {
      scheduler.rescheduleTask(config.autoFetchIntervalMinutes);
    }
    currentInterval = config.autoFetchIntervalMinutes;
  }

  private void autoFetchDisabled() {
    currentInterval = 0;
    log.debug("Existing task cancelled on auto-fetch disable");
  }

  public void stateChanged(@NotNull AutoFetchState state) {
    if (isAutoFetchEnabled(state)) {
      scheduleTaskOnStateChange();
    }
  }

  private boolean isAutoFetchEnabled(@NotNull AutoFetchState state) {
    return state.canAutoFetch() && autoFetchEnabled.get();
  }

  private void scheduleTaskOnStateChange() {
    int delayMinutes = calculateTaskDelayMinutesOnStateChange();
    scheduler.scheduleTask(delayMinutes);
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
    long nextAutoFetch = lastAutoFetch + TimeUnit.MINUTES.toMillis(currentInterval);
    long difference = nextAutoFetch - System.currentTimeMillis();
    if (difference > 0) {
      return Math.max((int) TimeUnit.MILLISECONDS.toMinutes(difference), DEFAULT_DELAY_MINUTES);
    } else {
      return DEFAULT_DELAY_MINUTES;
    }
  }

  public Project project() {
    return project;
  }

  private boolean isActive() {
    return active.get();
  }

  @Override
  public long lastAutoFetch() {
    return scheduler.getLastAutoFetchDate();
  }

  @Override
  public void projectOpened() {
    if (active.compareAndSet(false, true)) {
      initializeFirstTask();
    }
  }

  @Override
  public void projectClosed() {
    active.set(false);
  }
}
