package zielu.gittoolbox.status;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import git4idea.repo.GitRepository;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.cache.PerRepoInfoCache;
import zielu.gittoolbox.cache.PerRepoStatusCacheListener;
import zielu.gittoolbox.cache.RepoInfo;
import zielu.gittoolbox.compat.Notifier;
import zielu.gittoolbox.ui.StatusMessages;

public class BehindWatcher extends AbstractProjectComponent {
    private final Logger LOG = Logger.getInstance(getClass());
    private final AtomicBoolean myActive = new AtomicBoolean();
    private final AtomicBoolean myShowNotification = new AtomicBoolean();
    private final Map<GitRepository, RepoInfo> myState = new ConcurrentHashMap<GitRepository, RepoInfo>();

    private MessageBusConnection myConnection;

    public BehindWatcher(Project project) {
        super(project);
    }

    public static BehindWatcher getInstance(@NotNull Project project) {
        return project.getComponent(BehindWatcher.class);
    }

    @Override
    public void initComponent() {
        myConnection = myProject.getMessageBus().connect();
        myConnection.subscribe(PerRepoInfoCache.CACHE_CHANGE, new PerRepoStatusCacheListener() {
            @Override
            public void initialized(ImmutableMap<GitRepository, RepoInfo> info) {
                myState.putAll(info);
                myShowNotification.set(true);
                LOG.debug("Initialized");
            }

            @Override
            public void stateChanged(@NotNull RepoInfo info,
                                     @NotNull GitRepository repository) {
                onStateChange(repository, info);
            }
        });
    }

    private String prepareMessage() {
        Map<GitRepository, RevListCount> statuses = Maps.newHashMap();
        for (Entry<GitRepository, RepoInfo> entry : myState.entrySet()) {
            RepoInfo value = entry.getValue();
            if (value.count.isPresent()) {
                statuses.put(entry.getKey(), value.count.get().behind);
            }
        }
        return StatusMessages.getInstance().prepareBehindMessage(statuses);
    }

    private void showNotification() {
        String message = prepareMessage();
        Notifier.getInstance(myProject).notifySuccess(message);
    }

    private void onStateChange(@NotNull GitRepository repository, @NotNull RepoInfo info) {
        RepoInfo previousInfo = myState.put(repository, info);
        if (myShowNotification.compareAndSet(true, false)) {
            showNotification();
        } else {
            if (previousInfo != null) {
                if (!previousInfo.status.sameRemoteHash(info.status)) {
                    showNotification();
                }
            } else {
                showNotification();
            }
        }
    }

    @Override
    public void disposeComponent() {
        myConnection.disconnect();
        myState.clear();
    }

    @Override
    public void projectOpened() {
        myActive.compareAndSet(false, true);
    }

    @Override
    public void projectClosed() {
        myActive.compareAndSet(true, false);
        myShowNotification.set(false);
    }
}
