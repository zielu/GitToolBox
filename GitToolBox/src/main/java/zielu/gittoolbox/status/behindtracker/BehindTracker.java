package zielu.gittoolbox.status.behindtracker;

import com.google.common.collect.ImmutableMap;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import git4idea.repo.GitRepository;
import git4idea.util.GitUIUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import jodd.util.StringBand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.cache.RepoInfo;
import zielu.gittoolbox.repo.GtRepository;
import zielu.gittoolbox.status.BehindStatus;
import zielu.gittoolbox.status.GitAheadBehindCount;
import zielu.gittoolbox.ui.behindtracker.BehindTrackerUi;
import zielu.gittoolbox.util.AppUtil;
import zielu.gittoolbox.util.Html;

class BehindTracker implements ProjectComponent {
  private final Logger log = Logger.getInstance(getClass());
  private final AtomicBoolean active = new AtomicBoolean();
  private final Map<GitRepository, RepoInfo> state = new HashMap<>();
  private final Map<GitRepository, PendingChange> pendingChanges = new HashMap<>();
  private final BehindTrackerUi ui;

  BehindTracker(@NotNull BehindTrackerUi ui) {
    this.ui = ui;
  }

  @NotNull
  static BehindTracker getInstance(@NotNull Project project) {
    return AppUtil.getComponentInstance(project, BehindTracker.class);
  }

  private Optional<BehindMessage> prepareMessage(
      @NotNull ImmutableMap<GitRepository, PendingChange> changes) {
    Map<GitRepository, BehindStatus> statuses = mapStateAsStatuses(changes);
    if (statuses.isEmpty() || allAreInvisible(changes)) {
      return Optional.empty();
    } else {
      statuses = removeZeros(statuses);
      if (statuses.isEmpty()) {
        return Optional.empty();
      } else {
        return Optional.of(createBehindMessage(statuses));
      }
    }
  }

  private synchronized Map<GitRepository, BehindStatus> mapStateAsStatuses(
      @NotNull ImmutableMap<GitRepository, PendingChange> changes) {
    Map<GitRepository, BehindStatus> statuses = new HashMap<>();
    changes.forEach((repo, change) -> statuses.put(repo, change.status));
    return statuses;
  }

  private boolean allAreInvisible(@NotNull ImmutableMap<GitRepository, PendingChange> changes) {
    for (PendingChange change : changes.values()) {
      if (change.type.isVisible()) {
        return false;
      }
    }
    return true;
  }

  private Map<GitRepository, BehindStatus> removeZeros(@NotNull Map<GitRepository, BehindStatus> statuses) {
    return statuses.entrySet().stream()
        .filter(entry -> entry.getValue().behind() > 0)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private BehindMessage createBehindMessage(Map<GitRepository, BehindStatus> statuses) {
    boolean manyReposInProject = hasManyReposInProject();
    boolean manyReposInStatuses = statuses.size() > 1;
    return new BehindMessage(ui.getStatusMessages().prepareBehindMessage(statuses, manyReposInProject),
        manyReposInStatuses);
  }

  private boolean hasManyReposInProject() {
    return state.size() > 1;
  }

  private void showNotification(@NotNull ImmutableMap<GitRepository, PendingChange> changes) {
    Optional<BehindMessage> messageOption = prepareMessage(changes);
    if (messageOption.isPresent() && active.get()) {
      showNotification(messageOption.get(), ChangeType.FETCHED);
    }
  }

  private void showNotification(@NotNull BehindMessage message, @NotNull ChangeType changeType) {
    StringBand finalMessage = formatMessage(message, changeType);
    ui.displaySuccessNotification(finalMessage.toString());
  }

  @NotNull
  private StringBand formatMessage(@NotNull BehindMessage message, @NotNull ChangeType changeType) {
    return new StringBand(GitUIUtil.bold(changeType.title()))
        .append(" (").append(Html.link("update", ResBundle.message("update.project")))
        .append(")").append(Html.BRX).append(message.text);
  }

  void onStateChange(@NotNull GitRepository repository, @NotNull RepoInfo info) {
    synchronized (this) {
      onStateChangeUnsafe(repository, info);
    }
  }

  private void onStateChangeUnsafe(@NotNull GitRepository repository, @NotNull RepoInfo info) {
    RepoInfo previousInfo = state.put(repository, info);
    if (log.isDebugEnabled()) {
      GtRepository repo = ui.getGtRepository(repository);
      log.debug("Info update [", repo.getName(), "]: ", previousInfo, " > ", info);
    }
    ChangeType changeType = detectChangeType(previousInfo, info);
    if (changeType == ChangeType.FETCHED) {
      BehindStatus status = null;
      if (previousInfo == null) {
        status = calculateBehindStatus(info, count -> BehindStatus.create(count.behind));
      } else if (info.maybeCount().isPresent()) {
        status = calculateBehindStatus(info, count -> calculateBehindStatus(previousInfo, count));
      }
      if (status != null) {
        pendingChanges.put(repository, new PendingChange(status, changeType));
      }
    } else if (changeType != ChangeType.NONE) {
      pendingChanges.remove(repository);
    }
  }

  private BehindStatus calculateBehindStatus(@NotNull RepoInfo info,
                                             @NotNull Function<GitAheadBehindCount, BehindStatus> operation) {
    return info.maybeCount()
        .filter(GitAheadBehindCount::isNotZeroBehind)
        .map(operation)
        .orElseGet(BehindStatus::empty);
  }

  private BehindStatus calculateBehindStatus(@Nullable RepoInfo previous, @NotNull GitAheadBehindCount currentCount) {
    int oldBehind = Optional.ofNullable(previous).flatMap(RepoInfo::maybeCount)
        .map(count -> count.behind.value()).orElse(0);
    int delta = currentCount.behind.value() - oldBehind;
    return BehindStatus.create(currentCount.behind, delta);
  }

  private ChangeType detectChangeType(@Nullable RepoInfo previous, @NotNull RepoInfo current) {
    ChangeType type = ChangeType.NONE;
    if (previous != null) {
      type = detectChangeTypeIfBothPresent(previous, current);
    }
    return type;
  }

  @NotNull
  private ChangeType detectChangeTypeIfBothPresent(@NotNull RepoInfo previous, @NotNull RepoInfo current) {
    if (isSameParentBranch(previous, current)) {
      return detectChangeTypeIfSameRemoteBranch(previous, current);
    } else {
      return ChangeType.SWITCHED;
    }
  }

  private ChangeType detectChangeTypeIfSameRemoteBranch(@NotNull RepoInfo previous, @NotNull RepoInfo current) {
    if (isLocalBranchSwitched(previous, current)) {
      return ChangeType.SWITCHED;
    } else if (isParentHashChanged(previous, current)) {
      return ChangeType.FETCHED;
    } else {
      return ChangeType.NONE;
    }
  }

  private boolean isSameParentBranch(@NotNull RepoInfo previous, @NotNull RepoInfo current) {
    return previous.status().sameParentBranch(current.status());
  }

  private boolean isParentHashChanged(@NotNull RepoInfo previous, @NotNull RepoInfo current) {
    return !previous.status().sameParentHash(current.status());
  }

  private boolean isLocalBranchSwitched(@NotNull RepoInfo previous, @NotNull RepoInfo current) {
    return !previous.status().sameLocalBranch(current.status());
  }

  void showChangeNotification() {
    if (ui.isNotificationEnabled()) {
      ImmutableMap<GitRepository, PendingChange> changes = drainChanges();
      log.debug("Show notification for ", changes.size(), " repositories");
      showNotification(changes);
    }
  }

  private synchronized ImmutableMap<GitRepository, PendingChange> drainChanges() {
    ImmutableMap<GitRepository, PendingChange> changedRepos = ImmutableMap.copyOf(pendingChanges);
    pendingChanges.clear();
    return changedRepos;
  }

  @Override
  public void projectOpened() {
    active.compareAndSet(false, true);
  }

  @Override
  public void disposeComponent() {
    synchronized (this) {
      disposeUnsafe();
    }
  }

  private void disposeUnsafe() {
    state.clear();
    pendingChanges.clear();
  }

  @Override
  public void projectClosed() {
    active.compareAndSet(true, false);
  }

  private enum ChangeType {
    NONE(false, "NONE"),
    @Deprecated
    HIDDEN(false, "HIDDEN"),
    FETCHED(true, ResBundle.message("message.fetch.done")),
    SWITCHED(true, ResBundle.message("message.switched"));

    private final boolean visible;
    private final String title;

    ChangeType(boolean visible, String title) {
      this.visible = visible;
      this.title = title;
    }

    boolean isVisible() {
      return visible;
    }

    String title() {
      return title;
    }
  }

  private static class PendingChange {
    public final BehindStatus status;
    public final ChangeType type;

    private PendingChange(BehindStatus status, ChangeType type) {
      this.status = status;
      this.type = type;
    }
  }

  private static class BehindMessage {
    public final String text;
    public final boolean manyRepos;

    private BehindMessage(String text, boolean manyRepos) {
      this.text = text;
      this.manyRepos = manyRepos;
    }
  }
}
