package zielu.gittoolbox.cache;

import com.google.common.base.MoreObjects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.status.GitAheadBehindCount;

public class RepoInfo {
    private static final RepoInfo empty = new RepoInfo(RepoStatus.empty(), null);

    @NotNull
    public final RepoStatus status;
    @Nullable
    public final GitAheadBehindCount count;

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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("status", status)
            .add("count", count)
            .toString();
    }
}
