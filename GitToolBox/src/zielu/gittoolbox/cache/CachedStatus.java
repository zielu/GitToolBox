package zielu.gittoolbox.cache;

import com.google.common.base.Optional;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.vcs.log.Hash;
import git4idea.GitLocalBranch;
import git4idea.GitRemoteBranch;
import git4idea.repo.GitRepository;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.status.GitStatusCalculator;
import zielu.gittoolbox.status.RevListCount;

public class CachedStatus {
    private final Logger LOG = Logger.getInstance(getClass());

    private Optional<RevListCount> behindStatus = Optional.absent();
    private GitLocalBranch branch;
    private GitRemoteBranch remote;
    private Hash remoteHash;

    private CachedStatus() {}

    public static CachedStatus create() {
        return new CachedStatus();
    }

    public synchronized Optional<RevListCount> update(@NotNull GitRepository repo, @NotNull GitStatusCalculator calculator) {
        final boolean debug = LOG.isDebugEnabled();

        boolean stale = false;
        GitLocalBranch currentBranch = repo.getCurrentBranch();
        if (debug) {
            LOG.debug("Branch: cached=" + branch + ", current=" + currentBranch);
        }
        if (!Objects.equals(branch, currentBranch)) {
            branch = currentBranch;
            stale = true;
        }
        if (currentBranch != null) {
            GitRemoteBranch currentRemote = currentBranch.findTrackedBranch(repo);
            if (debug) {
                LOG.debug("Remote branch: cached=" + remote + ", current=" + currentRemote);
            }
            if (!Objects.equals(remote, currentRemote)) {
                remote = currentRemote;
                stale = true;
            }
            if (currentRemote != null) {
                Hash currentRemoteHash = currentRemote.getHash();
                if (debug) {
                    LOG.debug("Remote hash: cached=" + remoteHash + ", current=" + currentRemoteHash);
                }
                if (!Objects.equals(remoteHash, currentRemoteHash)) {
                    remoteHash = currentRemoteHash;
                    stale = true;
                }
            } else {
                remoteHash = null;
            }
        } else {
            remote = null;
            remoteHash = null;
        }

        if (stale) {
            Optional<RevListCount> oldBehindStatus = behindStatus;
            behindStatus = Optional.of(calculator.behindStatus(repo));
            if (debug) {
                LOG.debug("Updated stale behind status: " + oldBehindStatus + " > " + behindStatus);
            }
        } else {
            if (debug) {
                LOG.debug("Behind status did not change: " + behindStatus);
            }
        }
        return behindStatus;
    }
}
