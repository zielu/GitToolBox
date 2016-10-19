package zielu.gittoolbox.cache;

import com.google.common.base.Objects;
import com.intellij.openapi.diagnostic.Logger;
import git4idea.repo.GitRepository;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.status.GitAheadBehindCount;
import zielu.gittoolbox.status.GitStatusCalculator;
import zielu.gittoolbox.util.LogWatch;

class CachedStatus {
    private final Logger LOG = Logger.getInstance(getClass());
    private final LogWatch statusUpdateWatch = LogWatch.create(LOG, "Status update");
    private final LogWatch repoStatusCreateWatch = LogWatch.create(LOG, "Repo status create");

    @Nullable
    private GitAheadBehindCount myCount;
    private RepoStatus myStatus;
    private RepoInfo myInfo = RepoInfo.empty();

    private CachedStatus() {
    }

    public static CachedStatus create() {
        return new CachedStatus();
    }

    @NotNull
    public synchronized Optional<RepoInfo> update(@NotNull GitRepository repo, @NotNull GitStatusCalculator calculator) {
        final boolean debug = LOG.isDebugEnabled();
        repoStatusCreateWatch.start();
        RepoStatus currentStatus = RepoStatus.create(repo);
        repoStatusCreateWatch.finish();
        if (debug) {
            LOG.debug("Current state: " + currentStatus);
        }

        if (!Objects.equal(myStatus, currentStatus)) {
            GitAheadBehindCount oldCount = myCount;
            statusUpdateWatch.start();
            myCount = calculator.aheadBehindStatus(repo);
            statusUpdateWatch.finish();
            if (debug) {
                LOG.debug("Updated stale status: " + oldCount + " > " + myCount);
            }
            myStatus = currentStatus;
            myInfo = RepoInfo.create(myStatus, myCount);
            return Optional.of(myInfo);
        } else {
            if (debug) {
                LOG.debug("Status did not change: " + myCount);
            }
            return Optional.empty();
        }
    }

    @NotNull
    public synchronized RepoInfo get() {
        return myInfo;
    }
}
