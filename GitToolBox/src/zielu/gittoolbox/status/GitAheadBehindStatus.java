package zielu.gittoolbox.status;

import zielu.gittoolbox.UtfSeq;

public class GitAheadBehindStatus {
    public final RevListCount ahead;
    public final RevListCount behind;

    private GitAheadBehindStatus(RevListCount _ahead, RevListCount _behind) {
        ahead = _ahead;
        behind = _behind;
    }

    public static GitAheadBehindStatus create(RevListCount ahead, RevListCount behind) {
        return new GitAheadBehindStatus(ahead, behind);
    }

    @Override
    public String toString() {
        return ahead + UtfSeq.arrowUp +" "+behind+UtfSeq.arrowDown;
    }

    public static GitAheadBehindStatus noRemote() {
        return create(RevListCount.noRemote(), RevListCount.noRemote());
    }
}
