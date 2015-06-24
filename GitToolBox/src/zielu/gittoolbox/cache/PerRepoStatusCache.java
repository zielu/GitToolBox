package zielu.gittoolbox.cache;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.messages.Topic;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryChangeListener;
import java.util.concurrent.ConcurrentMap;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.status.GitStatusCalculator;
import zielu.gittoolbox.status.RevListCount;

public class PerRepoStatusCache implements GitRepositoryChangeListener, Disposable {
    public static Topic<PerRepoStatusCacheListener> CACHE_CHANGE = Topic.create("Status cache change", PerRepoStatusCacheListener.class);

    private final ConcurrentMap<GitRepository, CachedStatus> behindStatuses = Maps.newConcurrentMap();
    private final Project myProject;
    private final GitStatusCalculator myCalculator;
    private final MessageBusConnection myRepoChangeConnection;

    private PerRepoStatusCache(@NotNull Project project) {
        myProject = project;
        myCalculator = GitStatusCalculator.create(project);
        myRepoChangeConnection = myProject.getMessageBus().connect();
        myRepoChangeConnection.subscribe(GitRepository.GIT_REPO_CHANGE, this);
    }

    public static PerRepoStatusCache create(@NotNull Project project) {
        return new PerRepoStatusCache(project);
    }

    public Optional<RevListCount> get(GitRepository repo) {
        CachedStatus cachedStatus = behindStatuses.get(repo);
        if (cachedStatus == null) {
            CachedStatus newStatus = CachedStatus.create();
            CachedStatus foundStatus = behindStatuses.putIfAbsent(repo, newStatus);
            cachedStatus = foundStatus != null ? foundStatus : newStatus;
        }
        return cachedStatus.update(repo, myCalculator);
    }

    @Override
    public void dispose() {
        myRepoChangeConnection.disconnect();
        behindStatuses.clear();
    }

    @Override
    public void repositoryChanged(@NotNull GitRepository gitRepository) {
        final GitRepository repo = gitRepository;
        behindStatuses.remove(repo);
        ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
            @Override
            public void run() {
                myProject.getMessageBus().syncPublisher(CACHE_CHANGE).stateChanged(repo);
            }
        });
    }
}
