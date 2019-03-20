package zielu.gittoolbox.fetch;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.config.GitToolBoxConfigForProject;

class AutoFetchSchedule {
  private static final Duration DEFAULT_DELAY = Duration.ofMinutes(1);

  private final AtomicLong lastAutoFetchTimestamp = new AtomicLong();
  private int currentIntervalMinutes;

  AutoFetchSchedule(@NotNull GitToolBoxConfigForProject config) {
    if (config.autoFetch) {
      currentIntervalMinutes = config.autoFetchIntervalMinutes;
    }
  }

  void updateLastAutoFetchDate() {
    lastAutoFetchTimestamp.set(System.currentTimeMillis());
  }

  long getLastAutoFetchDate() {
    return lastAutoFetchTimestamp.get();
  }

  Duration updateAutoFetchIntervalMinutes(int newInterval) {
    Duration duration;
    if (currentIntervalMinutes == 0) {
      duration = Duration.ofSeconds(45);
    } else {
      duration = Duration.ofMinutes(newInterval);
    }
    currentIntervalMinutes = newInterval;
    return duration;
  }

  Duration getInitTaskDelay() {
    return Duration.ofSeconds(30);
  }

  void autoFetchDisabled() {
    currentIntervalMinutes = 0;
  }

  Duration calculateTaskDelayOnStateChange() {
    long lastAutoFetch = lastAutoFetchTimestamp.get();
    if (lastAutoFetch != 0) {
      return calculateDelayMinutesIfTaskWasExecuted(lastAutoFetch);
    } else {
      return DEFAULT_DELAY;
    }
  }

  private Duration calculateDelayMinutesIfTaskWasExecuted(long lastAutoFetch) {
    long nextAutoFetch = lastAutoFetch + TimeUnit.MINUTES.toMillis(currentIntervalMinutes);
    long difference = nextAutoFetch - System.currentTimeMillis();
    if (difference > 0) {
      long delay = Math.max(difference, DEFAULT_DELAY.toMillis());
      return Duration.ofMillis(delay);
    } else {
      return DEFAULT_DELAY;
    }
  }

  Duration getInterval() {
    return Duration.ofMinutes(currentIntervalMinutes);
  }
}
