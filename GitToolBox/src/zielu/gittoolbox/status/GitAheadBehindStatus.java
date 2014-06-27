package zielu.gittoolbox.status;

import zielu.gittoolbox.UtfSeq;

public class GitAheadBehindStatus {
    public final int ahead;
    public final int behind;

    private GitAheadBehindStatus(int _ahead, int _behind) {
        ahead = _ahead;
        behind = _behind;
    }

    public static GitAheadBehindStatus create(int ahead, int behind) {
        return new GitAheadBehindStatus(ahead, behind);
    }

    public static GitAheadBehindStatus empty() {
        return create(0, 0);
    }

    @Override
    public String toString() {
        return ahead + UtfSeq.ArrowUp+" "+behind+UtfSeq.ArrowDown;
    }
}
