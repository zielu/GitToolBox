package zielu.gittoolbox.cache;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.util.Optional;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.status.GitAheadBehindCount;

public class RepoInfo {
  private static final RepoInfo EMPTY = new RepoInfo(RepoStatus.empty(), null);
  @NotNull
  private final RepoStatus status;
  @Nullable
  private final GitAheadBehindCount count;

  private RepoInfo(@NotNull RepoStatus status, @Nullable GitAheadBehindCount count) {
    this.status = status;
    this.count = count;
  }

  public static RepoInfo create(@NotNull RepoStatus status, @Nullable GitAheadBehindCount count) {
    return new RepoInfo(status, count);
  }

  public static RepoInfo empty() {
    return EMPTY;
  }

  public RepoStatus status() {
    return status;
  }

  public Optional<GitAheadBehindCount> count() {
    return Optional.ofNullable(count);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
        .append("status", status)
        .append("count", count)
        .build();
  }
}
