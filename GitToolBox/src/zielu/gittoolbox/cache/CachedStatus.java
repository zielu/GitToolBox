package zielu.gittoolbox.cache;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.intellij.openapi.diagnostic.Logger;
import git4idea.repo.GitRepository;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.status.GitAheadBehindCount;
import zielu.gittoolbox.status.GitStatusCalculator;
import zielu.gittoolbox.util.LogWatch;

class CachedStatus {
    private final Logger LOG = Logger.getInstance(getClass());
    private final LogWatch statusUpdateWatch = LogWatch.create(LOG, "Status update");
    private final LogWatch repoStatusCreateWatch = LogWatch.create(LOG, "Repo status create");
    private final ReadWriteLock myLock = new ReentrantReadWriteLock();

    private Optional<GitAheadBehindCount> myCount = Optional.absent();
    private RepoStatus myStatus;
    private RepoInfo myInfo;

    private CachedStatus() {}

    public static CachedStatus create() {
        return new CachedStatus();
    }

    public void update(@NotNull GitRepository repo, @NotNull GitStatusCalculator calculator) {
        myLock.writeLock().lock();
        try {
            final boolean debug = LOG.isDebugEnabled();
            repoStatusCreateWatch.start();
            RepoStatus currentStatus = RepoStatus.create(repo);
            repoStatusCreateWatch.finish();
            if (debug) {
                LOG.debug("Current state: " + currentStatus);
            }

            if (!Objects.equal(myStatus, currentStatus)) {
                Optional<GitAheadBehindCount> oldCount = myCount;
                statusUpdateWatch.start();
                myCount = Optional.of(calculator.aheadBehindStatus(repo));
                statusUpdateWatch.finish();
                if (debug) {
                    LOG.debug("Updated stale status: " + oldCount + " > " + myCount);
                }
                myStatus = currentStatus;
                myInfo = RepoInfo.create(myStatus, myCount);
            } else {
                if (debug) {
                    LOG.debug("Status did not change: " + myCount);
                }
            }
        } finally {
            myLock.writeLock().unlock();
        }
    }

    public RepoInfo get() {
        myLock.readLock().lock();
        RepoInfo result = this.myInfo;
        myLock.readLock().unlock();
        return result;
    }
}
