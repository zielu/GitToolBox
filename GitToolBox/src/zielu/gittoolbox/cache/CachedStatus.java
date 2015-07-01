package zielu.gittoolbox.cache;

import com.google.common.base.Optional;
import com.intellij.openapi.diagnostic.Logger;
import git4idea.repo.GitRepository;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.status.GitStatusCalculator;
import zielu.gittoolbox.status.RevListCount;

public class CachedStatus {
    private final Logger LOG = Logger.getInstance(getClass());

    private Optional<RevListCount> behindStatus = Optional.absent();
    private RepoStatus state;

    private CachedStatus() {}

    public static CachedStatus create() {
        return new CachedStatus();
    }

    public synchronized Optional<RevListCount> update(@NotNull GitRepository repo, @NotNull GitStatusCalculator calculator) {
        final boolean debug = LOG.isDebugEnabled();

        RepoStatus currentState = RepoStatus.create(repo);
        if (debug) {
            LOG.debug("Current state: " + currentState);
        }

        if (!Objects.equals(state, currentState)) {
            Optional<RevListCount> oldBehindStatus = behindStatus;
            behindStatus = Optional.of(calculator.behindStatus(repo));
            if (debug) {
                LOG.debug("Updated stale behind status: " + oldBehindStatus + " > " + behindStatus);
            }
            state = currentState;
        } else {
            if (debug) {
                LOG.debug("Behind status did not change: " + behindStatus);
            }
        }
        return behindStatus;
    }
}
