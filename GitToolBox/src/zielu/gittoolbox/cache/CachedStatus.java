package zielu.gittoolbox.cache;

import com.google.common.base.Optional;
import com.intellij.vcs.log.Hash;
import git4idea.GitLocalBranch;
import git4idea.GitRemoteBranch;
import git4idea.repo.GitRepository;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.status.GitStatusCalculator;
import zielu.gittoolbox.status.RevListCount;

public class CachedStatus {
    private Optional<RevListCount> behindStatus = Optional.absent();
    private GitLocalBranch branch;
    private GitRemoteBranch remote;
    private Hash remoteHash;

    private CachedStatus() {}

    public static CachedStatus create() {
        return new CachedStatus();
    }

    public Optional<RevListCount> update(@NotNull GitRepository repo, @NotNull GitStatusCalculator calculator) {
        boolean stale = false;
        GitLocalBranch currentBranch = repo.getCurrentBranch();
        if (!Objects.equals(branch, currentBranch)) {
            branch = currentBranch;
            stale = true;
        }
        if (currentBranch != null) {
            GitRemoteBranch currentRemote = currentBranch.findTrackedBranch(repo);
            if (!Objects.equals(remote, currentRemote)) {
                remote = currentRemote;
                stale = true;
            }
            if (currentRemote != null) {
                if (!Objects.equals(remoteHash, currentRemote.getHash())) {
                    remoteHash = currentRemote.getHash();
                    stale = true;
                }
            } else {
                remoteHash = null;
            }
        } else {
            remote = null;
        }

        if (stale) {
            behindStatus = Optional.of(calculator.behindStatus(repo));
        }
        return behindStatus;
    }
}
