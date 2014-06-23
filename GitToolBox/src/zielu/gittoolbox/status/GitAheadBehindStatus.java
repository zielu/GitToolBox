package zielu.gittoolbox.status;

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
        return "\u2191"+ahead+" \u2193"+behind;    
    }
}
