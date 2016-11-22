package zielu.gittoolbox.cache;

import com.google.common.base.Objects;
import com.intellij.openapi.diagnostic.Logger;
import git4idea.repo.GitRepository;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.status.GitAheadBehindCount;
import zielu.gittoolbox.status.GitStatusCalculator;
import zielu.gittoolbox.util.GtUtil;
import zielu.gittoolbox.util.LogWatch;

class CachedStatus {
    private static final Logger LOG = Logger.getInstance(CachedStatus.class);
    private final LogWatch statusUpdateWatch = LogWatch.create(LOG, "Status update");
    private final LogWatch repoStatusCreateWatch = LogWatch.create(LOG, "Repo status create");
    private final AtomicBoolean myInvalid = new AtomicBoolean(true);
    private final AtomicBoolean myNew = new AtomicBoolean(true);
    private final String myRepoName;
    @Nullable
    private GitAheadBehindCount myCount;
    private RepoStatus myStatus;
    private RepoInfo myInfo = RepoInfo.empty();

    private CachedStatus(String repoName) {
        myRepoName = repoName;
    }

    public static CachedStatus create(GitRepository repository) {
        return new CachedStatus(GtUtil.name(repository));
    }

    @NotNull
    public synchronized Optional<RepoInfo> update(@NotNull GitRepository repo, @NotNull GitStatusCalculator calculator) {
        final boolean debug = LOG.isDebugEnabled();
        myNew.set(false);
        if (myInvalid.get()) {
            repoStatusCreateWatch.start();
            RepoStatus currentStatus = RepoStatus.create(repo);
            repoStatusCreateWatch.finish();
            if (debug) {
                LOG.debug("State update [" + myRepoName + "]:\nnew=" + currentStatus + "\nold=" + myStatus);
            }

            if (!Objects.equal(myStatus, currentStatus)) {
                GitAheadBehindCount oldCount = myCount;
                statusUpdateWatch.start();
                myCount = calculator.aheadBehindStatus(repo, currentStatus.localHash(), currentStatus.remoteHash());
                statusUpdateWatch.finish();
                if (debug) {
                    LOG.debug("Updated stale status [" + myRepoName + "]: " + oldCount + " > " + myCount);
                }
                if (!currentStatus.sameHashes(myCount)) {
                    LOG.warn("Hash mismatch between count and status: " + myCount + " <> " + currentStatus);
                }
                myStatus = currentStatus;
                myInfo = RepoInfo.create(myStatus, myCount);
                myInvalid.set(false);
                return Optional.of(myInfo);
            } else {
                if (debug) {
                    LOG.debug("Status did not change [" + myRepoName + "]: " + myCount);
                }
                return Optional.empty();
            }
        } else {
            if (debug) {
                LOG.debug("Status is still valid [" + myRepoName + "]: " + myCount);
            }
            return Optional.empty();
        }
    }

    public void invalidate() {
        myInvalid.set(true);
    }

    public boolean isInvalid() {
        return myInvalid.get();
    }

    public boolean isNew() {
        return myNew.get();
    }

    @NotNull
    public synchronized RepoInfo get() {
        return myInfo;
    }
}
