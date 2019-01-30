package zielu.gittoolbox.cache;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import com.intellij.vcs.log.Hash;
import git4idea.GitRemoteBranch;
import java.util.Objects;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.Nullable;

public final class RepoStatusRemote {
  private static final RepoStatusRemote empty = new RepoStatusRemote(null, null, null);

  private final GitRemoteBranch remoteTrackingBranch;
  private final GitRemoteBranch parentBranch;
  private final Hash parentHash;

  public RepoStatusRemote(GitRemoteBranch remoteTrackingBranch, GitRemoteBranch parentBranch, Hash parentHash) {
    this.remoteTrackingBranch = remoteTrackingBranch;
    this.parentBranch = parentBranch;
    this.parentHash = parentHash;
  }

  public RepoStatusRemote(GitRemoteBranch remoteTrackingBranch, Hash parentHash) {
    this(remoteTrackingBranch, remoteTrackingBranch, parentHash);
  }

  public static RepoStatusRemote empty() {
    return empty;
  }

  @Nullable
  public Hash parentHash() {
    return parentHash;
  }

  @Nullable
  public GitRemoteBranch parentBranch() {
    return parentBranch;
  }

  public boolean sameParentHash(RepoStatusRemote other) {
    return Objects.equals(parentHash, other.parentHash);
  }

  public boolean sameParentBranch(RepoStatusRemote other) {
    return Objects.equals(parentBranch, other.parentBranch);
  }

  public boolean isTrackingRemote() {
    return remoteTrackingBranch != null;
  }

  public boolean isParentSameAsTracking() {
    return Objects.equals(remoteTrackingBranch, parentBranch);
  }

  public boolean isEmpty() {
    return remoteTrackingBranch == null
        && parentBranch == null
        && parentHash == null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    RepoStatusRemote that = (RepoStatusRemote) o;

    return new EqualsBuilder()
        .append(remoteTrackingBranch, that.remoteTrackingBranch)
        .append(parentBranch, that.parentBranch)
        .append(parentHash, that.parentHash)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return Objects.hash(remoteTrackingBranch, parentBranch, parentHash);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
        .append("remoteTrackingBranch", remoteTrackingBranch)
        .append("parentBranch", parentBranch)
        .append("parentHash", parentHash)
        .build();
  }
}
