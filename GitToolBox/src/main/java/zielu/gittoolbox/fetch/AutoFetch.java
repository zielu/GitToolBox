package zielu.gittoolbox.fetch;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.config.GitToolBoxConfigPrj;

class AutoFetch implements AutoFetchComponent, Disposable {
  private final Logger log = Logger.getInstance(getClass());

  private final AtomicBoolean active = new AtomicBoolean();
  private final AtomicBoolean autoFetchEnabled = new AtomicBoolean();
  private final Project project;

  AutoFetch(@NotNull Project project) {
    this.project = project;
    Disposer.register(project, this);
  }

  private GitToolBoxConfigPrj getConfig() {
    return GitToolBoxConfigPrj.getInstance(project());
  }

  private void updateAutoFetchEnabled(GitToolBoxConfigPrj config) {
    autoFetchEnabled.set(config.getAutoFetch());
    executor().setAutoFetchEnabled(autoFetchEnabled.get());
  }

  private AutoFetchExecutor executor() {
    return AutoFetchExecutor.getInstance(project);
  }

  private void initializeFirstTask() {
    updateAutoFetchEnabled(getConfig());
    if (autoFetchEnabled.get()) {
      log.debug("Auto-fetch on project ready for ", project);
      scheduleFirstTask();
    } else {
      log.debug("Auto-fetch on project ready disabled for ", project);
    }
  }

  private void scheduleFirstTask() {
    log.debug("Schedule first auto-fetch for ", project);
    executor().scheduleTask(schedule().getInitTaskDelay());
  }

  private AutoFetchSchedule schedule() {
    return AutoFetchSchedule.getInstance(project);
  }

  @Override
  public void configChanged(@NotNull GitToolBoxConfigPrj previous,
                            @NotNull GitToolBoxConfigPrj current) {
    updateAutoFetchEnabled(current);
    if (autoFetchEnabled.get()) {
      log.debug("Auto-fetch enabled");
      autoFetchEnabled(previous, current);
    } else {
      log.debug("Auto-fetch disabled");
      autoFetchDisabled();
    }
  }

  private void autoFetchEnabled(@NotNull GitToolBoxConfigPrj previous,
                                @NotNull GitToolBoxConfigPrj current) {
    if (current.getAutoFetchIntervalMinutes() != previous.getAutoFetchIntervalMinutes()) {
      autoFetchIntervalChanged(current);
    } else {
      log.debug("Auto-fetch interval did not change: interval=", current.getAutoFetchIntervalMinutes());
    }
  }

  private void autoFetchIntervalChanged(@NotNull GitToolBoxConfigPrj config) {
    log.debug("Auto-fetch interval or state changed: enabled=", config.getAutoFetch(),
        ", interval=", config.getAutoFetchIntervalMinutes());
    Duration taskDelay = schedule().updateAutoFetchIntervalMinutes(config.getAutoFetchIntervalMinutes());
    executor().rescheduleTask(taskDelay);
  }

  private void autoFetchDisabled() {
    schedule().autoFetchDisabled();
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
    log.debug("Schedule auto-fetch on state change for ", project);
    executor().scheduleTask(schedule().calculateTaskDelayOnStateChange());
  }

  public Project project() {
    return project;
  }

  @Override
  public long lastAutoFetch() {
    return schedule().getLastAutoFetchDate();
  }

  @Override
  public void projectReady() {
    if (active.compareAndSet(false, true)) {
      initializeFirstTask();
    }
  }

  @Override
  public void dispose() {
    active.compareAndSet(true, false);
  }
}
