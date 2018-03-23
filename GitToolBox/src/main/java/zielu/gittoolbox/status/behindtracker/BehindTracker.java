package zielu.gittoolbox.status.behindtracker;

import com.google.common.collect.ImmutableSet;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import git4idea.repo.GitRepository;
import git4idea.util.GitUIUtil;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import javax.swing.event.HyperlinkEvent;
import jodd.util.StringBand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.cache.RepoInfo;
import zielu.gittoolbox.compat.Notifier;
import zielu.gittoolbox.config.GitToolBoxConfig;
import zielu.gittoolbox.status.BehindStatus;
import zielu.gittoolbox.status.GitAheadBehindCount;
import zielu.gittoolbox.ui.StatusMessages;
import zielu.gittoolbox.ui.UpdateProject;
import zielu.gittoolbox.util.GtUtil;
import zielu.gittoolbox.util.Html;

public class BehindTracker implements ProjectComponent {
  private final Logger log = Logger.getInstance(getClass());
  private final AtomicBoolean active = new AtomicBoolean();
  private final Map<GitRepository, RepoInfo> state = new HashMap<>();
  private final Map<GitRepository, PendingChange> pendingChanges = new HashMap<>();
  private final Project project;
  private final NotificationListener updateProjectListener;

  public BehindTracker(@NotNull Project project) {
    this.project = project;
    updateProjectListener = new NotificationListener.Adapter() {
      @Override
      protected void hyperlinkActivated(@NotNull Notification notification, @NotNull HyperlinkEvent hyperlinkEvent) {
        UpdateProject.create(project).execute();
      }
    };
  }

  @NotNull
  public static BehindTracker getInstance(@NotNull Project project) {
    return project.getComponent(BehindTracker.class);
  }

  private Optional<BehindMessage> prepareMessage(@NotNull Collection<GitRepository> repositories) {
    Map<GitRepository, BehindStatus> statuses = mapStateAsStatuses(repositories);

    if (statuses.isEmpty()) {
      return Optional.empty();
    } else {
      return Optional.of(createBehindMessage(statuses));
    }
  }

  private synchronized Map<GitRepository, BehindStatus> mapStateAsStatuses(
      @NotNull Collection<GitRepository> repositories) {

    Map<GitRepository, BehindStatus> statuses = new HashMap<>();
    repositories.forEach(repo -> {
      RepoInfo info = state.getOrDefault(repo, RepoInfo.empty());
      info.count().filter(GitAheadBehindCount::isNotZero).ifPresent(count -> statuses.put(repo,
          BehindStatus.create(count.behind)));
    });
    return statuses;
  }

  private BehindMessage createBehindMessage(Map<GitRepository, BehindStatus> statuses) {
    boolean manyReposInProject = hasManyReposInProject();
    boolean manyReposInStatuses = statuses.size() > 1;
    return new BehindMessage(StatusMessages.getInstance().prepareBehindMessage(statuses, manyReposInProject),
        manyReposInStatuses);
  }

  private boolean hasManyReposInProject() {
    return state.size() > 1;
  }

  private void showNotification(@NotNull Collection<GitRepository> repositories) {
    Optional<BehindMessage> messageOption = prepareMessage(repositories);
    if (messageOption.isPresent() && active.get()) {
      showNotification(messageOption.get(), ChangeType.FETCHED);
    }
  }

  private void showNotification(@NotNull BehindMessage message, @NotNull ChangeType changeType) {
    StringBand finalMessage = formatMessage(message, changeType);
    displaySuccessNotification(finalMessage);
  }

  private void displaySuccessNotification(StringBand message) {
    Notifier.getInstance(project).behindTrackerSuccess(message.toString(), updateProjectListener);
  }

  @NotNull
  private StringBand formatMessage(@NotNull BehindMessage message, @NotNull ChangeType changeType) {
    return new StringBand(GitUIUtil.bold(changeType.title()))
        .append(" (").append(Html.link("update", ResBundle.getString("update.project")))
        .append(")").append(Html.BR).append(message.text);
  }

  private boolean isNotificationEnabled() {
    return GitToolBoxConfig.getInstance().behindTracker;
  }

  void onStateChange(@NotNull GitRepository repository, @NotNull RepoInfo info) {
    synchronized (this) {
      onStateChangeUnsafe(repository, info);
    }
  }

  private void onStateChangeUnsafe(@NotNull GitRepository repository, @NotNull RepoInfo info) {
    RepoInfo previousInfo = state.put(repository, info);
    if (log.isDebugEnabled()) {
      log.debug("Info update [", GtUtil.name(repository), "]: ", previousInfo, " > ", info);
    }
    ChangeType changeType = detectChangeType(previousInfo, info);
    PendingChange oldPending = pendingChanges.remove(repository);
    if (changeType.isVisible()) {
      BehindStatus status = null;
      if (oldPending == null) {
        status = calculateBehindStatus(info, count -> BehindStatus.create(count.behind));
      } else if (info.count().isPresent()) {
        status = calculateBehindStatus(info, count -> calculateBehindStatus(previousInfo, count));
      }
      Optional.ofNullable(status)
          .ifPresent(stat -> pendingChanges.put(repository, new PendingChange(stat, changeType)));
    }
  }

  private BehindStatus calculateBehindStatus(@NotNull RepoInfo info,
                                             @NotNull Function<GitAheadBehindCount, BehindStatus> operation) {
    return info.count()
        .filter(GitAheadBehindCount::isNotZeroBehind)
        .map(operation)
        .orElseGet(BehindStatus::empty);
  }

  private BehindStatus calculateBehindStatus(@Nullable RepoInfo previous, @NotNull GitAheadBehindCount currentCount) {
    int oldBehind = Optional.ofNullable(previous).flatMap(RepoInfo::count)
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
    if (isSameRemoteBranch(previous, current)) {
      return detectChangeTypeIfSameRemoteBranch(previous, current);
    }
    return ChangeType.NONE;
  }

  private ChangeType detectChangeTypeIfSameRemoteBranch(@NotNull RepoInfo previous, @NotNull RepoInfo current) {
    if (isRemoteHashChanged(previous, current)) {
      return ChangeType.FETCHED;
    } else {
      return ChangeType.NONE;
    }
  }

  private boolean isSameRemoteBranch(@NotNull RepoInfo previous, @NotNull RepoInfo current) {
    return previous.status().sameRemoteBranch(current.status());
  }

  private boolean isRemoteHashChanged(@NotNull RepoInfo previous, @NotNull RepoInfo current) {
    return !previous.status().sameRemoteHash(current.status());
  }

  @Deprecated
  private boolean isLocalBranchSwitched(@NotNull RepoInfo previous, @NotNull RepoInfo current) {
    return !previous.status().sameLocalBranch(current.status());
  }

  void showChangeNotification() {
    if (isNotificationEnabled()) {
      Collection<GitRepository> changedRepos = drainChangedRepos();
      log.debug("Show notification for ", changedRepos.size(), " repositories");
      showNotification(changedRepos);
    }
  }

  private synchronized Collection<GitRepository> drainChangedRepos() {
    Collection<GitRepository> changedRepos = ImmutableSet.copyOf(pendingChanges.keySet());
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
    FETCHED(true, ResBundle.getString("message.fetch.done")),
    @Deprecated
    SWITCHED(true, ResBundle.getString("message.switched"));

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
