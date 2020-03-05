package zielu.gittoolbox.cache;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.status.GitAheadBehindCount;

public class RepoInfo {
  private static final RepoInfo EMPTY = new RepoInfo(RepoStatus.empty(), null, ImmutableList.of());
  @NotNull
  private final RepoStatus status;
  @Nullable
  private final GitAheadBehindCount count;
  @NotNull
  private final ImmutableList<String> tags;

  private RepoInfo(@NotNull RepoStatus status, @Nullable GitAheadBehindCount count, @NotNull List<String> tags) {
    this.status = status;
    this.count = count;
    this.tags = ImmutableList.copyOf(tags);
  }

  public static RepoInfo create(@NotNull RepoStatus status, @Nullable GitAheadBehindCount count,
                                  @NotNull List<String> tags) {
    return new RepoInfo(status, count, tags);
  }

  public static RepoInfo empty() {
    return EMPTY;
  }

  @NotNull
  public RepoStatus status() {
    return status;
  }

  public Optional<GitAheadBehindCount> maybeCount() {
    return Optional.ofNullable(count);
  }

  @Nullable
  public GitAheadBehindCount count() {
    return count;
  }

  public ImmutableList<String> tags() {
    return tags;
  }

  public boolean tagsNotEmpty() {
    return !tags.isEmpty();
  }

  public boolean isEmpty() {
    return status.isEmpty() && count == null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RepoInfo that = (RepoInfo) o;
    return Objects.equals(status, that.status)
        && Objects.equals(Sets.newHashSet(tags), Sets.newHashSet(that.tags));
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, tags);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
        .append("status", status)
        .append("count", count)
        .append("tags", tags)
        .build();
  }
}
