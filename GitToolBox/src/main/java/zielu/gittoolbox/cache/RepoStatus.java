package zielu.gittoolbox.cache;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import com.google.common.base.Objects;
import com.intellij.vcs.log.Hash;
import git4idea.GitLocalBranch;
import git4idea.GitRemoteBranch;
import git4idea.repo.GitRepoInfo;
import git4idea.repo.GitRepository;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.status.GitAheadBehindCount;

public class RepoStatus {
  private static final RepoStatus empty = new RepoStatus(null, null, null, null);

  private final GitLocalBranch localBranch;
  private final Hash localHash;
  private final GitRemoteBranch remoteBranch;
  private final Hash remoteHash;

  private RepoStatus(GitLocalBranch localBranch, Hash localHash, GitRemoteBranch remoteBranch, Hash remoteHash) {
    this.localBranch = localBranch;
    this.localHash = localHash;
    this.remoteBranch = remoteBranch;
    this.remoteHash = remoteHash;
  }

  public static RepoStatus create(@NotNull GitRepository repository) {
    Hash localHash = null;
    GitRemoteBranch remote = null;
    Hash remoteHash = null;

    GitLocalBranch localBranch = getLocalBranch(repository);
    if (localBranch != null) {
      GitRepoInfo repoInfo = repository.getInfo();
      localHash = repoInfo.getLocalBranchesWithHashes().get(localBranch);
      remote = localBranch.findTrackedBranch(repository);
      remoteHash = repoInfo.getRemoteBranchesWithHashes().get(remote);
    }

    return new RepoStatus(localBranch, localHash, remote, remoteHash);
  }

  public static RepoStatus create(GitLocalBranch localBranch, Hash localHash,
                                  GitRemoteBranch remoteBranch, Hash remoteHash) {
    return new RepoStatus(localBranch, localHash, remoteBranch, remoteHash);
  }

  @Nullable
  private static GitLocalBranch getLocalBranch(@NotNull GitRepository repository) {
    return repository.getCurrentBranch();
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

  public boolean sameLocalBranch(RepoStatus other) {
    return Objects.equal(localBranch, other.localBranch);
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

  public boolean isEmpty() {
    return localHash == null
        && localBranch == null
        && remoteHash == null
        && remoteBranch == null;
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
    return new EqualsBuilder()
        .append(localBranch, that.localBranch)
        .append(localHash, that.localHash)
        .append(remoteBranch, that.remoteBranch)
        .append(remoteHash, that.remoteHash)
        .build();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(localBranch, localHash, remoteBranch, remoteHash);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
        .append("localHash", localHash)
        .append("remoteHash", remoteHash)
        .append("localBranch", localBranch)
        .append("remoteBranch", remoteBranch)
        .build();
  }
}
