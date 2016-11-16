package zielu.gittoolbox.cache;

import com.google.common.collect.Maps;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.messages.Topic;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryChangeListener;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.ProjectAware;
import zielu.gittoolbox.status.GitStatusCalculator;

public class PerRepoInfoCache implements GitRepositoryChangeListener, Disposable, ProjectAware {
    public static final Topic<PerRepoStatusCacheListener> CACHE_CHANGE = Topic.create("Status cache change", PerRepoStatusCacheListener.class);

    private final Logger LOG = Logger.getInstance(getClass());

    private final AtomicBoolean myActive = new AtomicBoolean();
    private final ConcurrentMap<GitRepository, CachedStatus> behindStatuses = Maps.newConcurrentMap();
    private final Application myApplication;
    private final Project myProject;
    private final GitStatusCalculator myCalculator;
    private final MessageBusConnection myRepoChangeConnection;
    private final Set<String> inUpdate = new ConcurrentSkipListSet<>();

    private PerRepoInfoCache(@NotNull Application application, @NotNull Project project) {
        myApplication = application;
        myProject = project;
        myCalculator = GitStatusCalculator.create(project);
        myRepoChangeConnection = myProject.getMessageBus().connect();
        myRepoChangeConnection.subscribe(GitRepository.GIT_REPO_CHANGE, this);
    }

    public static PerRepoInfoCache create(@NotNull Project project) {
        return new PerRepoInfoCache(ApplicationManager.getApplication(), project);
    }

    private CachedStatus get(final GitRepository repository) {
        CachedStatus cachedStatus = behindStatuses.get(repository);
        if (cachedStatus == null) {
            CachedStatus newStatus = CachedStatus.create(repository);
            CachedStatus foundStatus = behindStatuses.putIfAbsent(repository, newStatus);
            cachedStatus = foundStatus != null ? foundStatus : newStatus;
        }
        return cachedStatus;
    }

    private CachedStatus updateAndGet(final GitRepository repository) {
        if (myActive.get()) {
            CachedStatus cachedStatus = get(repository);
            String repoKey = repository.getRoot().getPath();
            if (inUpdate.add(repoKey)) {
                myApplication.runReadAction(() -> {
                    update(repository, cachedStatus);
                    inUpdate.remove(repoKey);
                });
            }
            return cachedStatus;
        } else {
            return CachedStatus.create(repository);
        }
    }

    private void update(GitRepository repository, CachedStatus status) {
        Optional<RepoInfo> updated = status.update(repository, myCalculator);
        updated.ifPresent(repoInfo -> onRepoChanged(repository, repoInfo));
    }

    @NotNull
    public RepoInfo getInfo(GitRepository repository) {
        CachedStatus cachedStatus = updateAndGet(repository);
        return cachedStatus.get();
    }

    @Override
    public void dispose() {
        myRepoChangeConnection.disconnect();
        behindStatuses.clear();
        inUpdate.clear();
    }

    @Override
    public void repositoryChanged(@NotNull GitRepository gitRepository) {
        final GitRepository repo = gitRepository;
        if (LOG.isDebugEnabled()) {
            LOG.debug("Got repo changed event: " + repo);
        }
        if (myActive.get()) {
            myApplication.executeOnPooledThread(() -> onRepoAsyncChanged(repo));
        }
    }

    private void onRepoAsyncChanged(GitRepository repo) {
        if (myActive.get()) {
            synchronized (this) {
                get(repo).invalidate();
                getInfo(repo);
            }
        }
    }

    private void onRepoChanged(GitRepository repo, RepoInfo info) {
        if (myActive.get()) {
            myProject.getMessageBus().syncPublisher(CACHE_CHANGE).stateChanged(info, repo);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Published cache changed event: " + repo);
            }
        }
    }

    @Override
    public void opened() {
        myActive.set(true);
    }

    @Override
    public void closed() {
        myActive.set(false);
    }
}
