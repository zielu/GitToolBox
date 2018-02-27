package zielu.gittoolbox.status.behindtracker;

import com.google.common.collect.Maps;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import git4idea.repo.GitRepository;
import git4idea.util.GitUIUtil;
import java.util.Map;
import java.util.Map.Entry;
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

  private Optional<BehindMessage> prepareMessage() {
    Map<GitRepository, RevListCount> statuses = mapStateAsStatuses();
    if (statuses.isEmpty()) {
      return Optional.empty();
    } else {
      return Optional.of(createBehindMessage(statuses));
    }
  }

  private Map<GitRepository, RevListCount> mapStateAsStatuses() {
    Map<GitRepository, RevListCount> statuses = Maps.newHashMap();
    for (Entry<GitRepository, RepoInfo> entry : state.entrySet()) {
      entry.getValue().count().filter(GitAheadBehindCount::isNotZero)
          .ifPresent(count -> statuses.put(entry.getKey(), count.behind));
    }
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

  private void showNotification(@NotNull ChangeType changeType) {
    Optional<BehindMessage> messageOption = prepareMessage();
    if (messageOption.isPresent() && active.get()) {
      showNotification(messageOption.get(), changeType);
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
    ChangeType type = detectChangeType(previousInfo, info);
    if (shouldShowNotification(type)) {
      showNotification(type);
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
    } else {
      return ChangeType.SWITCHED;
    }
  }

  private ChangeType detectChangeTypeIfSameRemoteBranch(@NotNull RepoInfo previous, @NotNull RepoInfo current) {
    if (isLocalBranchSwitched(previous, current)) {
      return ChangeType.SWITCHED;
    } else if (isRemoteHashChanged(previous, current)) {
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

  private boolean isLocalBranchSwitched(@NotNull RepoInfo previous, @NotNull RepoInfo current) {
    return !previous.status().sameLocalBranch(current.status());
  }

  private boolean shouldShowNotification(ChangeType changeType) {
    return changeType.isVisible() && isNotificationEnabled();
  }

  @Override
  public void disposeComponent() {
    state.clear();
  }

  @Override
  public void projectOpened() {
    active.compareAndSet(false, true);
  }

  @Override
  public void projectClosed() {
    if (active.compareAndSet(true, false)) {
      state.clear();
    }
  }

  private enum ChangeType {
    NONE(false, "NONE"),
    HIDDEN(false, "HIDDEN"),
    FETCHED(true, ResBundle.getString("message.fetch.done")),
    SWITCHED(true, ResBundle.getString("message.switched"));

    private final boolean myVisible;
    private final String myTitle;

    ChangeType(boolean visible, String title) {
      myVisible = visible;
      this.myTitle = title;
    }

    boolean isVisible() {
      return myVisible;
    }

    String title() {
      return myTitle;
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
