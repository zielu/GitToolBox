package zielu.gittoolbox.fetch;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.config.GitToolBoxConfigForProject;

public class AutoFetch implements ProjectComponent, AutoFetchComponent {
  private final Logger log = Logger.getInstance(getClass());

  private final AtomicBoolean active = new AtomicBoolean();
  private final AtomicBoolean autoFetchEnabled = new AtomicBoolean();
  private final Project project;
  private final AutoFetchExecutor executor;
  private final AutoFetchSchedule schedule;

  AutoFetch(@NotNull Project project, @NotNull AutoFetchExecutor executor, @NotNull AutoFetchSchedule schedule) {
    this.project = project;
    this.executor = executor;
    this.schedule = schedule;
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
    executor.setAutoFetchEnabled(autoFetchEnabled.get());
  }

  @NotNull
  public static AutoFetch getInstance(@NotNull Project project) {
    return project.getComponent(AutoFetch.class);
  }

  private void initializeFirstTask() {
    if (autoFetchEnabled.get()) {
      scheduleFirstTask();
    }
  }

  private void scheduleFirstTask() {
    executor.scheduleTask(schedule.getInitTaskDelay());
  }

  @Override
  public void configChanged(@NotNull GitToolBoxConfigForProject previous,
                            @NotNull GitToolBoxConfigForProject current) {
    updateAutoFetchEnabled(current);
    if (autoFetchEnabled.get()) {
      log.debug("Auto-fetch enabled");
      autoFetchEnabled(previous, current);
    } else {
      log.debug("Auto-fetch disabled");
      autoFetchDisabled();
    }
  }

  private void autoFetchEnabled(@NotNull GitToolBoxConfigForProject previous,
                                @NotNull GitToolBoxConfigForProject current) {
    if (current.isAutoFetchIntervalMinutesChanged(previous.autoFetchIntervalMinutes)) {
      autoFetchIntervalChanged(current);
    } else {
      log.debug("Auto-fetch interval did not change: interval=", current.autoFetchIntervalMinutes);
    }
  }

  private void autoFetchIntervalChanged(@NotNull GitToolBoxConfigForProject config) {
    log.debug("Auto-fetch interval or state changed: enabled=", config.autoFetch,
        ", interval=", config.autoFetchIntervalMinutes);
    Duration taskDelay = schedule.updateAutoFetchIntervalMinutes(config.autoFetchIntervalMinutes);
    executor.rescheduleTask(taskDelay);
  }

  private void autoFetchDisabled() {
    schedule.autoFetchDisabled();
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
    executor.scheduleTask(schedule.calculateTaskDelayOnStateChange());
  }

  public Project project() {
    return project;
  }

  @Override
  public long lastAutoFetch() {
    return schedule.getLastAutoFetchDate();
  }

  @Override
  public void projectOpened() {
    if (active.compareAndSet(false, true)) {
      initializeFirstTask();
    }
  }

  @Override
  public void projectClosed() {
    active.compareAndSet(true, false);
  }
}
