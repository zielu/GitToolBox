package zielu.gittoolbox.cache;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import zielu.gittoolbox.status.GitAheadBehindCount;

public class RepoInfo {
    public final RepoStatus status;
    public final Optional<GitAheadBehindCount> count;

    private RepoInfo(RepoStatus _status, Optional<GitAheadBehindCount> _count) {
        status = _status;
        count = _count;
    }

    public static RepoInfo create(RepoStatus status, Optional<GitAheadBehindCount> count) {
        return new RepoInfo(status, count);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("status", status)
            .add("count", count)
            .toString();
    }
}
