package zielu.gittoolbox.ui;

import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.status.GitAheadBehindCount;
import zielu.gittoolbox.status.Status;
import zielu.gittoolbox.status.StatusMessages;

public enum StatusText {
    ;

    public static String format(GitAheadBehindCount aheadBehind) {
        String statusText = StatusMessages.aheadBehindStatus(aheadBehind);
        if (aheadBehind.status() == Status.Success) {
            return statusText;
        } else {
            return ResBundle.getString("git.na");
        }
    }
}
