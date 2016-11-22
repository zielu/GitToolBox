package zielu.gittoolbox.cache;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.messages.Topic;
import git4idea.GitUtil;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryChangeListener;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.ProjectAware;
import zielu.gittoolbox.status.GitStatusCalculator;

public class PerRepoInfoCache implements GitRepositoryChangeListener, Disposable, ProjectAware {
    public static final Topic<PerRepoStatusCacheListener> CACHE_CHANGE = Topic.create("Status cache change", PerRepoStatusCacheListener.class);

    private final Logger LOG = Logger.getInstance(getClass());

    private ExecutorService myUpdateExecutor;

    private final AtomicBoolean myActive = new AtomicBoolean();
    private final ConcurrentMap<GitRepository, CachedStatus> myBehindStatuses = Maps.newConcurrentMap();
    private final Application myApplication;
    private final Project myProject;
    private final GitStatusCalculator myCalculator;
    private final MessageBusConnection myRepoChangeConnection;

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
        CachedStatus cachedStatus = myBehindStatuses.get(repository);
        if (cachedStatus == null) {
            CachedStatus newStatus = CachedStatus.create(repository);
            CachedStatus foundStatus = myBehindStatuses.putIfAbsent(repository, newStatus);
            cachedStatus = foundStatus != null ? foundStatus : newStatus;
        }
        return cachedStatus;
    }

    private CachedStatus updateAndGet(final GitRepository repository) {
        if (myActive.get()) {
            CachedStatus cachedStatus = get(repository);
            myApplication.runReadAction(() -> update(repository, cachedStatus));
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
        myBehindStatuses.clear();
        myUpdateExecutor = null;
    }

    private void scheduleUpdate(@NotNull GitRepository repository) {
        final boolean debug = LOG.isDebugEnabled();
        if (myActive.get()) {
            if (debug) {
                LOG.debug("Scheduled update for: " + repository);
            }
            myUpdateExecutor.submit(new UpdateTask(repository));
        } else {
            if (debug) {
                LOG.debug("Inactive - ignored scheduling update for " + repository);
            }
        }
    }

    @Override
    public void repositoryChanged(@NotNull GitRepository repository) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Got repo changed event: " + repository);
        }
        scheduleUpdate(repository);
    }

    private void onRepoChanged(GitRepository repo, RepoInfo info) {
        if (myActive.get()) {
            myProject.getMessageBus().syncPublisher(CACHE_CHANGE).stateChanged(info, repo);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Published cache changed event: " + repo);
            }
        }
    }

    private void refreshNoSync(GitRepository repository) {
        get(repository).invalidate();
        getInfo(repository);
    }

    private synchronized void refresh(GitRepository repository) {
        refreshNoSync(repository);
    }

    public void refreshAll() {
        LOG.info("Refreshing repositories statuses");
        Collection<GitRepository> repositories = GitUtil.getRepositories(myProject);
        repositories.forEach(this::scheduleUpdate);
    }

    @Override
    public void opened() {
        if (myActive.compareAndSet(false,true)) {
            ThreadFactoryBuilder threadBuilder = new ThreadFactoryBuilder();
            myUpdateExecutor = Executors.newSingleThreadExecutor(
                    threadBuilder.setNameFormat(getClass().getSimpleName()+"-["+myProject.getName()+"]-%d").build()
            );
        }
    }

    @Override
    public void closed() {
        if (myActive.compareAndSet(true, false)) {
            myUpdateExecutor.shutdown();
        }
    }

    private class UpdateTask implements Runnable {
        private final GitRepository myRepository;

        private UpdateTask(@NotNull GitRepository myRepository) {
            this.myRepository = myRepository;
        }

        @Override
        public void run() {
            if (myActive.get()) {
                refresh(myRepository);
            }
        }
    }
}
