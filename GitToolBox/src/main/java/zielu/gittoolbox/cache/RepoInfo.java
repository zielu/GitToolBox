package zielu.gittoolbox.cache;

import com.google.common.base.MoreObjects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.status.GitAheadBehindCount;

import java.util.Optional;

public class RepoInfo {
    private static final RepoInfo empty = new RepoInfo(RepoStatus.empty(), null);
    @NotNull
    private final RepoStatus status;
    @Nullable
    private final GitAheadBehindCount count;

    private RepoInfo(@NotNull RepoStatus _status, @Nullable GitAheadBehindCount _count) {
        status = _status;
        count = _count;
    }

    public static RepoInfo create(@NotNull RepoStatus status, @Nullable GitAheadBehindCount count) {
        return new RepoInfo(status, count);
    }

    public static RepoInfo empty() {
        return empty;
    }

    public RepoStatus status() {
        return status;
    }

    public Optional<GitAheadBehindCount> count() {
        return Optional.ofNullable(count);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("status", status)
            .add("count", count)
            .toString();
    }
}
