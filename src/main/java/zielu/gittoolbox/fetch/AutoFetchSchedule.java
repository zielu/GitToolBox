package zielu.gittoolbox.fetch;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import git4idea.repo.GitRepository;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.config.GitToolBoxConfigPrj;
import zielu.gittoolbox.config.ProjectConfig;
import zielu.gittoolbox.util.AppUtil;

class AutoFetchSchedule implements Disposable {
  private final Logger log = Logger.getInstance(getClass());
  private static final Duration DEFAULT_DELAY = Duration.ofMinutes(1);
  private static final Duration BRANCH_SWITCH_GRACE_PERIOD = Duration.ofSeconds(30);
  private final Map<GitRepository, AtomicLong> lastFetchTimestamps = new ConcurrentHashMap<>();
  private final AtomicLong lastFetchTimestamp = new AtomicLong();
  private final Project project;
  private volatile Integer currentIntervalMinutes;

  AutoFetchSchedule(@NotNull Project project) {
    this.project = project;
  }

  @NotNull
  static AutoFetchSchedule getInstance(@NotNull Project project) {
    return AppUtil.getServiceInstance(project, AutoFetchSchedule.class);
  }

  @NotNull
  static Optional<AutoFetchSchedule> getExistingServiceInstance(@NotNull Project project) {
    return AppUtil.getExistingServiceInstance(project, AutoFetchSchedule.class);
  }

  @Override
  public void dispose() {
    lastFetchTimestamps.clear();
  }

  void repositoriesRemoved(@NotNull Collection<GitRepository> repositories) {
    repositories.forEach(lastFetchTimestamps::remove);
  }

  void updateLastAutoFetchDate(@NotNull Collection<GitRepository> repositories) {
    long timestamp = getNowTimestamp();
    repositories.forEach(repo -> getLastFetch(repo).set(timestamp));
  }

  private long getNowTimestamp() {
    return AutoFetchFacade.getInstance(project).getNowMillis();
  }

  private AtomicLong getLastFetch(@NotNull GitRepository repository) {
    return lastFetchTimestamps.computeIfAbsent(repository, newRepo -> new AtomicLong());
  }

  void updateLastCyclicAutoFetchDate(@NotNull Collection<GitRepository> repositories) {
    lastFetchTimestamp.set(getNowTimestamp());
    updateLastAutoFetchDate(repositories);
  }

  long getLastAutoFetchDate() {
    return lastFetchTimestamp.get();
  }

  Duration updateAutoFetchIntervalMinutes(int newInterval) {
    Duration duration;
    int currentIntervalMin = getCurrentIntervalMinutes();
    if (currentIntervalMin == 0) {
      duration = Duration.ofSeconds(45);
    } else {
      duration = Duration.ofMinutes(newInterval);
    }
    setCurrentIntervalMinutes(newInterval);
    return duration;
  }

  private int getCurrentIntervalMinutes() {
    if (currentIntervalMinutes == null) {
      synchronized (this) {
        if (currentIntervalMinutes == null) {
          GitToolBoxConfigPrj config = ProjectConfig.get(project);
          if (config.getAutoFetch()) {
            currentIntervalMinutes = config.getAutoFetchIntervalMinutes();
          } else {
            currentIntervalMinutes = 0;
          }
        }
      }
    }
    return currentIntervalMinutes;
  }

  private void setCurrentIntervalMinutes(int minutes) {
    currentIntervalMinutes = minutes;
  }

  Duration getInitTaskDelay(int reposCount) {
    long secondsDuration = Math.round(Math.max(10, Math.log(reposCount) * 6 + 1));
    log.info("First auto-fetch delay is " + secondsDuration + " sec");
    return Duration.ofSeconds(secondsDuration);
  }

  void autoFetchDisabled() {
    setCurrentIntervalMinutes(0);
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
    long nextAutoFetch = lastAutoFetch + TimeUnit.MINUTES.toMillis(getCurrentIntervalMinutes());
    long difference = nextAutoFetch - System.currentTimeMillis();
    if (difference > 0) {
      long delay = Math.max(difference, DEFAULT_DELAY.toMillis());
      return Duration.ofMillis(delay);
    } else {
      return DEFAULT_DELAY;
    }
  }

  Duration getInterval() {
    return Duration.ofMinutes(getCurrentIntervalMinutes());
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
    long nextAutoFetch = lastAutoFetch + TimeUnit.MINUTES.toMillis(getCurrentIntervalMinutes());
    long difference = nextAutoFetch - System.currentTimeMillis();
    if (difference > BRANCH_SWITCH_GRACE_PERIOD.toMillis()) {
      return Duration.ofSeconds(3);
    } else {
      return Duration.ZERO;
    }
  }

  @NotNull
  List<GitRepository> filterUpdatedAroundNow(@NotNull Collection<GitRepository> repositories) {
    long now = getNowTimestamp();
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
