package zielu.gittoolbox.status;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import git4idea.repo.GitRepository;
import git4idea.util.GitUIUtil;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.event.HyperlinkEvent;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.GitToolBoxConfig;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.cache.PerRepoInfoCache;
import zielu.gittoolbox.cache.PerRepoStatusCacheListener;
import zielu.gittoolbox.cache.RepoInfo;
import zielu.gittoolbox.compat.Notifier;
import zielu.gittoolbox.ui.StatusMessages;
import zielu.gittoolbox.ui.UpdateProject;
import zielu.gittoolbox.util.GtUtil;
import zielu.gittoolbox.util.Html;

public class BehindTracker extends AbstractProjectComponent {
    private final Logger LOG = Logger.getInstance(getClass());
    private final AtomicBoolean myActive = new AtomicBoolean();
    private final Map<GitRepository, RepoInfo> myState = new ConcurrentHashMap<GitRepository, RepoInfo>();
    private final NotificationListener updateProjectListener;

    private MessageBusConnection myConnection;

    public BehindTracker(Project project) {
        super(project);
        updateProjectListener = new NotificationListener.Adapter() {
            @Override
            protected void hyperlinkActivated(@NotNull Notification notification, @NotNull HyperlinkEvent hyperlinkEvent) {
                UpdateProject.create(myProject).execute();
            }
        };
    }

    public static BehindTracker getInstance(@NotNull Project project) {
        return project.getComponent(BehindTracker.class);
    }

    @Override
    public void initComponent() {
        myConnection = myProject.getMessageBus().connect();
        myConnection.subscribe(PerRepoInfoCache.CACHE_CHANGE, new PerRepoStatusCacheListener() {
            @Override
            public void stateChanged(@NotNull RepoInfo info,
                                     @NotNull GitRepository repository) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("State changed ["+GtUtil.name(repository)+"]: " + info);
                }
                if (myActive.get()) {
                    onStateChange(repository, info);
                }
            }
        });
    }

    private Optional<BehindMessage> prepareMessage() {
        Map<GitRepository, RevListCount> statuses = Maps.newHashMap();
        for (Entry<GitRepository, RepoInfo> entry : myState.entrySet()) {
            RepoInfo value = entry.getValue();
            Optional<GitAheadBehindCount> countOption = value.count;
            if (countOption.isPresent()) {
                GitAheadBehindCount count = countOption.get();
                if (count.isNotZeroBehind()) {
                    statuses.put(entry.getKey(), count.behind);
                }
            }
        }
        if (!statuses.isEmpty()) {
            boolean manyReposInProject = myState.size() > 1;
            BehindMessage message = new BehindMessage(StatusMessages.getInstance()
                .prepareBehindMessage(statuses, manyReposInProject), statuses.size() > 1);
            return Optional.of(message);
        } else {
            return Optional.absent();
        }
    }

    private void showNotification(@NotNull  ChangeType changeType) {
        Optional<BehindMessage> messageOption = prepareMessage();
        if (messageOption.isPresent() && myActive.get()) {
            BehindMessage message = messageOption.get();
            String finalMessage = GitUIUtil.bold(changeType.title()) +
                " (" + Html.link("update", ResBundle.getString("update.project")) + ")" +
                Html.br +
                message.text;

            Notifier.getInstance(myProject).notifySuccess(finalMessage, updateProjectListener);
        }
    }

    private boolean isNotificationEnabled() {
        return GitToolBoxConfig.getInstance().behindTracker;
    }

    private boolean isRemoteHashChanged(RepoInfo previous, RepoInfo current) {
        return !previous.status.sameRemoteHash(current.status);
    }

    private boolean isBranchSwitched(RepoInfo previous, RepoInfo current) {
        return !previous.status.sameBranch(current.status);
    }

    private void onStateChange(@NotNull GitRepository repository, @NotNull RepoInfo info) {
        RepoInfo previousInfo = myState.put(repository, info);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Info update ["+GtUtil.name(repository)+"]: " + previousInfo + " > " + info);
        }
        ChangeType type = ChangeType.none;
        if (previousInfo != null) {
            if (previousInfo.status.sameRemoteBranch(info.status)) {
                if (isBranchSwitched(previousInfo, info)) {
                    type = ChangeType.switched;
                } else if (isRemoteHashChanged(previousInfo, info)) {
                    type = ChangeType.fetched;
                }
            } else {
                type = ChangeType.switched;
            }
        }
        if (type.isVisible() && isNotificationEnabled()) {
            showNotification(type);
        }
    }

    @Override
    public void disposeComponent() {
        myState.clear();
    }

    @Override
    public void projectOpened() {
        myActive.compareAndSet(false, true);
    }

    @Override
    public void projectClosed() {
        if (myActive.compareAndSet(true, false)) {
            myConnection.disconnect();
            myState.clear();
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

    private enum ChangeType {
        none(false, "NONE"),
        hidden(false, "HIDDEN"),
        fetched(true, ResBundle.getString("message.fetch.done")),
        switched(true, ResBundle.getString("message.switched"))
        ;

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
}
