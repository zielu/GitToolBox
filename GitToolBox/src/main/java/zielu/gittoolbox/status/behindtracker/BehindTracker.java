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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.event.HyperlinkEvent;
import jodd.util.StringBand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.cache.RepoInfo;
import zielu.gittoolbox.compat.Notifier;
import zielu.gittoolbox.config.GitToolBoxConfig;
import zielu.gittoolbox.status.GitAheadBehindCount;
import zielu.gittoolbox.status.RevListCount;
import zielu.gittoolbox.ui.StatusMessages;
import zielu.gittoolbox.ui.UpdateProject;
import zielu.gittoolbox.util.GtUtil;
import zielu.gittoolbox.util.Html;

public class BehindTracker implements ProjectComponent {
  private final Logger log = Logger.getInstance(getClass());
  private final AtomicBoolean active = new AtomicBoolean();
  private final Map<GitRepository, RepoInfo> state = new ConcurrentHashMap<>();
  private final Map<GitRepository, ChangeType> pendingChanges = new ConcurrentHashMap<>();
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
    Map<GitRepository, RevListCount> statuses = mapStateAsStatuses(repositories);

    if (statuses.isEmpty()) {
      return Optional.empty();
    } else {
      return Optional.of(createBehindMessage(statuses));
    }
  }

  private Map<GitRepository, RevListCount> mapStateAsStatuses(@NotNull Collection<GitRepository> repositories) {
    Map<GitRepository, RevListCount> statuses = new HashMap<>();
    repositories.forEach(repo -> {
      RepoInfo info = state.getOrDefault(repo, RepoInfo.empty());
      info.count().filter(GitAheadBehindCount::isNotZero).ifPresent(count -> statuses.put(repo, count.behind));
    });
    return statuses;
  }

  private BehindMessage createBehindMessage(Map<GitRepository, RevListCount> statuses) {
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
    RepoInfo previousInfo = state.put(repository, info);
    if (log.isDebugEnabled()) {
      log.debug("Info update [", GtUtil.name(repository), "]: ", previousInfo, " > ", info);
    }
    ChangeType changeType = detectChangeType(previousInfo, info);
    if (changeType.isVisible()) {
      pendingChanges.put(repository, changeType);
    }
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
      Collection<GitRepository> changedRepos = ImmutableSet.copyOf(pendingChanges.keySet());
      pendingChanges.clear();
      log.debug("Show notification for ", changedRepos.size(), " repositories");
      showNotification(changedRepos);
    }
  }

  @Override
  public void projectOpened() {
    active.compareAndSet(false, true);
  }

  @Override
  public void disposeComponent() {
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

  private static class BehindMessage {
    public final String text;
    public final boolean manyRepos;

    private BehindMessage(String text, boolean manyRepos) {
      this.text = text;
      this.manyRepos = manyRepos;
    }
  }
}
