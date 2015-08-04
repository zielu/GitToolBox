package zielu.gittoolbox.cache;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.intellij.openapi.diagnostic.Logger;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.status.GitAheadBehindCount;
import zielu.gittoolbox.status.GitStatusCalculator;
import zielu.gittoolbox.util.LogWatch;

public class CachedStatus {
    private final Logger LOG = Logger.getInstance(getClass());
    private final LogWatch statusUpdateWatch = LogWatch.create(LOG, "Status update");
    private final LogWatch repoStatusCreateWatch = LogWatch.create(LOG, "Repo status create");

    private Optional<GitAheadBehindCount> status = Optional.absent();
    private RepoStatus state;

    private CachedStatus() {}

    public static CachedStatus create() {
        return new CachedStatus();
    }

    public synchronized Optional<GitAheadBehindCount> update(@NotNull GitRepository repo, @NotNull GitStatusCalculator calculator) {
        final boolean debug = LOG.isDebugEnabled();
        repoStatusCreateWatch.start();
        RepoStatus currentState = RepoStatus.create(repo);
        repoStatusCreateWatch.finish();
        if (debug) {
            LOG.debug("Current state: " + currentState);
        }

        if (!Objects.equal(state, currentState)) {
            Optional<GitAheadBehindCount> oldBehindStatus = status;
            statusUpdateWatch.start();
            status = Optional.of(calculator.aheadBehindStatus(repo));
            statusUpdateWatch.finish();
            if (debug) {
                LOG.debug("Updated stale behind status: " + oldBehindStatus + " > " + status);
            }
            state = currentState;
        } else {
            if (debug) {
                LOG.debug("Behind status did not change: " + status);
            }
        }
        return status;
    }
}
