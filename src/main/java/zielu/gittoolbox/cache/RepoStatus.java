package zielu.gittoolbox.cache;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import com.intellij.vcs.log.Hash;
import git4idea.GitBranch;
import git4idea.GitLocalBranch;
import git4idea.GitRemoteBranch;
import java.util.Objects;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.status.GitAheadBehindCount;

public final class RepoStatus {
  private static final RepoStatus empty = new RepoStatus(null, null, RepoStatusRemote.empty());

  private final GitLocalBranch localBranch;
  private final Hash localHash;
  private final String localShortHash;
  private final RepoStatusRemote remote;

  private RepoStatus(GitLocalBranch localBranch, Hash localHash, RepoStatusRemote remote) {
    this.localBranch = localBranch;
    this.localHash = localHash;
    this.localShortHash = localHash == null ? null : localHash.toShortString();
    this.remote = remote;
  }

  public static RepoStatus create(GitLocalBranch localBranch, Hash localHash, RepoStatusRemote remote) {
    return new RepoStatus(localBranch, localHash, remote);
  }

  public static RepoStatus empty() {
    return empty;
  }

  @Nullable
  public Hash localHash() {
    return localHash;
  }

  @Nullable
  public String localShortHash() {
    return localShortHash;
  }

  @Nullable
  public Hash parentHash() {
    return remote.parentHash();
  }

  @Nullable
  public GitRemoteBranch parentBranch() {
    return remote.parentBranch();
  }

  @Nullable
  public GitBranch localBranch() {
    return localBranch;
  }

  @Nullable
  public GitRemoteBranch remoteBranch() {
    return remote.remoteBranch();
  }

  public boolean sameLocalBranch(RepoStatus other) {
    return Objects.equals(localBranch, other.localBranch);
  }

  public boolean sameParentHash(RepoStatus other) {
    return remote.sameParentHash(other.remote);
  }

  public boolean sameParentBranch(RepoStatus other) {
    return remote.sameParentBranch(other.remote);
  }

  public boolean isTrackingRemote() {
    return remote.isTrackingRemote();
  }

  public boolean isParentDifferentFromTracking() {
    return !remote.isParentSameAsTracking();
  }

  public boolean isNameMaster() {
    if (localBranch != null) {
      return "master".equals(localBranch.getName());
    }
    return false;
  }

  public boolean sameHashes(GitAheadBehindCount aheadBehind) {
    return Objects.equals(localHash, aheadBehind.getAhead().getTop())
               && Objects.equals(remote.parentHash(), aheadBehind.getBehind().getTop());
  }

  public boolean isEmpty() {
    return localHash == null
               && localBranch == null
               && remote.isEmpty();
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
               .append(remote, that.remote)
               .build();
  }

  @Override
  public int hashCode() {
    return Objects.hash(localBranch, localHash, remote);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
               .append("localHash", localHash)
               .append("localBranch", localBranch)
               .append("remote", remote)
               .build();
  }
}
