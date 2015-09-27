package zielu.gittoolbox.cache;

import com.google.common.base.Objects;
import com.intellij.vcs.log.Hash;
import git4idea.GitLocalBranch;
import git4idea.GitRemoteBranch;
import git4idea.repo.GitRepository;

public class RepoStatus {
    private final GitLocalBranch branch;
    private final Hash localHash;
    private final GitRemoteBranch remote;
    private final Hash remoteHash;

    private RepoStatus(GitLocalBranch branch, Hash localHash, GitRemoteBranch remote, Hash remoteHash) {
        this.branch = branch;
        this.localHash = localHash;
        this.remote = remote;
        this.remoteHash = remoteHash;
    }

    public static RepoStatus create(GitRepository repository) {
        GitLocalBranch branch;
        Hash localHash = null;
        GitRemoteBranch remote = null;
        Hash remoteHash = null;

        branch = repository.getCurrentBranch();
        if (branch != null) {
            localHash = branch.getHash();
            remote = branch.findTrackedBranch(repository);
            if (remote != null) {
                remoteHash = remote.getHash();
            }
        }
        return new RepoStatus(branch, localHash, remote, remoteHash);
    }

    public boolean sameRemoteHash(RepoStatus other) {
        return Objects.equal(remoteHash, other.remoteHash);
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
            Objects.equal(remote, that.remote) &&
            Objects.equal(remoteHash, that.remoteHash);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(branch, localHash, remote, remoteHash);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("branch", branch)
            .add("localHash", localHash)
            .add("remote", remote)
            .add("remoteHash", remoteHash)
            .toString();
    }
}
