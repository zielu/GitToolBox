package zielu.gittoolbox.status;

import zielu.gittoolbox.UtfSeq;

public class GitAheadBehindCount {
    public final RevListCount ahead;
    public final RevListCount behind;

    private GitAheadBehindCount(RevListCount _ahead, RevListCount _behind) {
        ahead = _ahead;
        behind = _behind;
    }

    private static GitAheadBehindCount create(RevListCount ahead, RevListCount behind) {
        return new GitAheadBehindCount(ahead, behind);
    }

    public static GitAheadBehindCount success(int ahead, int behind) {
        return create(RevListCount.success(ahead), RevListCount.success(behind));
    }

    public static GitAheadBehindCount cancel() {
        return create(RevListCount.cancel(), RevListCount.cancel());
    }

    public static GitAheadBehindCount failure() {
        return create(RevListCount.failure(), RevListCount.failure());
    }

    public boolean isNotZero() {
        if (status() == Status.Success) {
            return ahead.value() != 0 || behind.value() != 0;
        } else {
            return false;
        }
    }

    public Status status() {
        return ahead.status();
    }

    @Override
    public String toString() {
        return ahead + UtfSeq.arrowUp +" "+behind+UtfSeq.arrowDown;
    }

    public static GitAheadBehindCount noRemote() {
        return create(RevListCount.noRemote(), RevListCount.noRemote());
    }
}
