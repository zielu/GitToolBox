package zielu.gittoolbox.status;

import com.google.common.collect.ImmutableMap;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import git4idea.repo.GitRepository;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.cache.PerRepoInfoCache;
import zielu.gittoolbox.cache.PerRepoStatusCacheListener;
import zielu.gittoolbox.cache.RepoInfo;

public class BehindWatcher extends AbstractProjectComponent {
    private final AtomicBoolean myActive = new AtomicBoolean();
    private final AtomicBoolean myShowNotifiaction = new AtomicBoolean();
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
            public void initialized(ImmutableMap<GitRepository, RepoInfo> status) {

            }

            @Override
            public void stateChanged(@NotNull RepoInfo info,
                                     @NotNull GitRepository repository) {

            }
        });
    }

    @Override
    public void disposeComponent() {
        myConnection.disconnect();
    }

    @Override
    public void projectOpened() {
        myActive.compareAndSet(false, true);
    }

    @Override
    public void projectClosed() {
        myActive.compareAndSet(true, false);
    }
}
