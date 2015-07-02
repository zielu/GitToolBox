package zielu.gittoolbox.cache;

import com.google.common.base.Optional;
import com.intellij.openapi.diagnostic.Logger;
import git4idea.repo.GitRepository;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.status.GitAheadBehindCount;
import zielu.gittoolbox.status.GitStatusCalculator;

public class CachedStatus {
    private final Logger LOG = Logger.getInstance(getClass());

    private Optional<GitAheadBehindCount> status = Optional.absent();
    private RepoStatus state;

    private CachedStatus() {}

    public static CachedStatus create() {
        return new CachedStatus();
    }

    public synchronized Optional<GitAheadBehindCount> update(@NotNull GitRepository repo, @NotNull GitStatusCalculator calculator) {
        final boolean debug = LOG.isDebugEnabled();

        RepoStatus currentState = RepoStatus.create(repo);
        if (debug) {
            LOG.debug("Current state: " + currentState);
        }

        if (!Objects.equals(state, currentState)) {
            Optional<GitAheadBehindCount> oldBehindStatus = status;
            status = Optional.of(calculator.aheadBehindStatus(repo));
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
