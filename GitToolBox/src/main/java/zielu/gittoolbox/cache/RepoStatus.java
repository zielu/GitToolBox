package zielu.gittoolbox.cache;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.intellij.vcs.log.Hash;
import git4idea.GitLocalBranch;
import git4idea.GitRemoteBranch;
import git4idea.repo.GitRepoInfo;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.status.GitAheadBehindCount;
import zielu.gittoolbox.util.GitLock;
import zielu.gittoolbox.util.GtUtil;

public class RepoStatus {
    private static final RepoStatus empty = new RepoStatus(null, null, null, null);

    private final GitLocalBranch branch;
    private final Hash localHash;
    private final GitRemoteBranch remoteBranch;
    private final Hash remoteHash;

    private RepoStatus(GitLocalBranch branch, Hash localHash, GitRemoteBranch remoteBranch, Hash remoteHash) {
        this.branch = branch;
        this.localHash = localHash;
        this.remoteBranch = remoteBranch;
        this.remoteHash = remoteHash;
    }

    public static RepoStatus create(@NotNull GitRepository repository) {
        GitLocalBranch branch;
        Hash localHash = null;
        GitRemoteBranch remote = null;
        Hash remoteHash = null;

        GitLock lock = GtUtil.lock(repository);
        lock.readLock();
        try {
            branch = repository.getCurrentBranch();
            if (branch != null) {
                GitRepoInfo repoInfo = repository.getInfo();
                localHash = repoInfo.getLocalBranchesWithHashes().get(branch);
                remote = branch.findTrackedBranch(repository);
                remoteHash = repoInfo.getRemoteBranchesWithHashes().get(remote);
            }
        } finally {
            lock.readUnlock();
        }
        return new RepoStatus(branch, localHash, remote, remoteHash);
    }

    public static RepoStatus empty() {
        return empty;
    }

    @Nullable
    public Hash localHash() {
        return localHash;
    }

    @Nullable
    public Hash remoteHash() {
        return remoteHash;
    }

    public boolean sameBranch(RepoStatus other) {
        return Objects.equal(branch, other.branch);
    }

    public boolean sameRemoteHash(RepoStatus other) {
        return Objects.equal(remoteHash, other.remoteHash);
    }

    public boolean sameRemoteBranch(RepoStatus other) {
        return Objects.equal(remoteBranch, other.remoteBranch);
    }

    public boolean hasRemoteBranch() {
        return remoteBranch != null;
    }

    public boolean sameHashes(GitAheadBehindCount aheadBehind) {
        return Objects.equal(localHash, aheadBehind.ahead.top()) && Objects.equal(remoteHash, aheadBehind.behind.top());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RepoStatus that = (RepoStatus) o;
        return Objects.equal(branch, that.branch) &&
            Objects.equal(localHash, that.localHash) &&
            Objects.equal(remoteBranch, that.remoteBranch) &&
            Objects.equal(remoteHash, that.remoteHash);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(branch, localHash, remoteBranch, remoteHash);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("localHash", localHash)
            .add("remoteHash", remoteHash)
            .add("localBranch", branch)
            .add("remoteBranch", remoteBranch)
            .toString();
    }
}
