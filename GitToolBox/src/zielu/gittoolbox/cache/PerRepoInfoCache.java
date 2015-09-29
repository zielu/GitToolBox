package zielu.gittoolbox.cache;

import com.google.common.collect.Maps;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.messages.Topic;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryChangeListener;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.ProjectAware;
import zielu.gittoolbox.status.GitStatusCalculator;

public class PerRepoInfoCache implements GitRepositoryChangeListener, Disposable, ProjectAware {
    public static Topic<PerRepoStatusCacheListener> CACHE_CHANGE = Topic.create("Status cache change", PerRepoStatusCacheListener.class);

    private final Logger LOG = Logger.getInstance(getClass());

    private final ReadWriteLock myLock = new ReentrantReadWriteLock();
    private volatile boolean myActive;
    private final ConcurrentMap<GitRepository, CachedStatus> behindStatuses = Maps.newConcurrentMap();
    private final Project myProject;
    private final GitStatusCalculator myCalculator;
    private final MessageBusConnection myRepoChangeConnection;

    private PerRepoInfoCache(@NotNull Project project) {
        myProject = project;
        myCalculator = GitStatusCalculator.create(project);
        myRepoChangeConnection = myProject.getMessageBus().connect();
        myRepoChangeConnection.subscribe(GitRepository.GIT_REPO_CHANGE, this);
    }

    public static PerRepoInfoCache create(@NotNull Project project) {
        return new PerRepoInfoCache(project);
    }

    private CachedStatus get(GitRepository repository) {
        myLock.writeLock().lock();
        try {
            if (myActive) {
                Application application = ApplicationManager.getApplication();
                AccessToken read = application.acquireReadActionLock();
                CachedStatus cachedStatus = behindStatuses.get(repository);
                if (cachedStatus == null) {
                    CachedStatus newStatus = CachedStatus.create();
                    CachedStatus foundStatus = behindStatuses.putIfAbsent(repository, newStatus);
                    cachedStatus = foundStatus != null ? foundStatus : newStatus;
                }
                cachedStatus.update(repository, myCalculator);
                read.finish();
                return cachedStatus;
            } else {
                return CachedStatus.create();
            }
        } finally {
            myLock.writeLock().unlock();
        }
    }

    @NotNull
    public RepoInfo getInfo(GitRepository repository) {
        CachedStatus cachedStatus = get(repository);
        return cachedStatus.get();
    }

    @Override
    public void dispose() {
        myRepoChangeConnection.disconnect();
        behindStatuses.clear();
    }

    @Override
    public void repositoryChanged(@NotNull GitRepository gitRepository) {
        final GitRepository repo = gitRepository;
        if (LOG.isDebugEnabled()) {
            LOG.debug("Got repo changed event: " + repo);
        }
        if (myActive) {
            Application application = ApplicationManager.getApplication();
            application.executeOnPooledThread(new Runnable() {
                @Override
                public void run() {
                    onRepoChanged(repo);
                }
            });
        }
    }

    private void onRepoChanged(GitRepository repo) {
        if (myActive) {
            RepoInfo info = getInfo(repo);
            myProject.getMessageBus().syncPublisher(CACHE_CHANGE).stateChanged(info, repo);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Published cache changed event: " + repo);
            }
        }
    }

    @Override
    public void opened() {
        myLock.writeLock().lock();
        myActive = true;
        myLock.writeLock().unlock();
    }

    @Override
    public void closed() {
        myLock.writeLock().lock();
        myActive = false;
        myLock.writeLock().unlock();
    }
}
