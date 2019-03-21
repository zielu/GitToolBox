package zielu.gittoolbox.fetch;

import com.intellij.openapi.Disposable;
import git4idea.repo.GitRepository;
import java.time.Clock;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.ProjectGateway;
import zielu.gittoolbox.config.GitToolBoxConfigForProject;

class AutoFetchSchedule implements Disposable {
  private static final Duration DEFAULT_DELAY = Duration.ofMinutes(1);
  private static final Duration BRANCH_SWITCH_GRACE_PERIOD = Duration.ofSeconds(30);
  private final Map<GitRepository, AtomicLong> lastFetchTimestamps = new ConcurrentHashMap<>();
  private final AtomicLong lastFetchTimestamp = new AtomicLong();
  private final Clock clock = Clock.systemDefaultZone();
  private int currentIntervalMinutes;

  AutoFetchSchedule(@NotNull GitToolBoxConfigForProject config, @NotNull ProjectGateway gateway) {
    if (config.autoFetch) {
      currentIntervalMinutes = config.autoFetchIntervalMinutes;
    }
    gateway.disposeWithProject(this);
  }

  @Override
  public void dispose() {
    lastFetchTimestamps.clear();
  }

  void repositoriesRemoved(@NotNull Collection<GitRepository> repositories) {
    repositories.forEach(lastFetchTimestamps::remove);
  }

  void updateLastAutoFetchDate(@NotNull Collection<GitRepository> repositories) {
    long timestamp = clock.millis();
    repositories.forEach(repo -> getLastFetch(repo).set(timestamp));
  }

  private AtomicLong getLastFetch(@NotNull GitRepository repository) {
    return lastFetchTimestamps.computeIfAbsent(repository, newRepo -> new AtomicLong());
  }

  void updateLastCyclicAutoFetchDate(@NotNull Collection<GitRepository> repositories) {
    lastFetchTimestamp.set(clock.millis());
    updateLastAutoFetchDate(repositories);
  }

  long getLastAutoFetchDate() {
    return lastFetchTimestamp.get();
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
    long lastAutoFetch = lastFetchTimestamp.get();
    if (lastAutoFetch != 0) {
      return calculateDelayIfTaskWasExecuted(lastAutoFetch);
    } else {
      return DEFAULT_DELAY;
    }
  }

  private Duration calculateDelayIfTaskWasExecuted(long lastAutoFetch) {
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

  Duration calculateTaskDelayOnBranchSwitch(@NotNull GitRepository repository) {
    long lastAutoFetch = getLastFetch(repository).get();
    if (lastAutoFetch != 0) {
      return calculateDelayOnBranchSwitch(lastAutoFetch);
    } else {
      return Duration.ZERO;
    }
  }

  private Duration calculateDelayOnBranchSwitch(long lastAutoFetch) {
    long nextAutoFetch = lastAutoFetch + TimeUnit.MINUTES.toMillis(currentIntervalMinutes);
    long difference = nextAutoFetch - System.currentTimeMillis();
    if (difference > BRANCH_SWITCH_GRACE_PERIOD.toMillis()) {
      return Duration.ofSeconds(3);
    } else {
      return Duration.ZERO;
    }
  }

  @NotNull
  List<GitRepository> filterUpdatedAroundNow(@NotNull Collection<GitRepository> repositories) {
    long now = clock.millis();
    return repositories.stream()
        .filter(repo -> updatedSomeTimeAgo(repo, now))
        .collect(Collectors.toList());
  }

  private boolean updatedSomeTimeAgo(@NotNull GitRepository repository, long now) {
    long lastFetch = getLastFetch(repository).get();
    if (lastFetch == 0) {
      return true;
    } else {
      long elapsedSinceUpdateMillis = now - lastFetch;
      return elapsedSinceUpdateMillis > BRANCH_SWITCH_GRACE_PERIOD.toMillis();
    }
  }
}
