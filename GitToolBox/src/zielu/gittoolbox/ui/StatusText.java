package zielu.gittoolbox.ui;

import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.status.GitAheadBehindCount;
import zielu.gittoolbox.status.Status;

public enum StatusText {
    ;

    public static String format(@NotNull GitAheadBehindCount aheadBehind) {
        if (aheadBehind.status() == Status.Success) {
            return StatusMessages.getInstance().aheadBehindStatus(aheadBehind);
        } else {
            return ResBundle.getString("git.na");
        }
    }

    public static String formatToolTip(@NotNull GitAheadBehindCount aheadBehind) {
        if (aheadBehind.status() == Status.Success) {
            return "";
        } else {
            return StatusMessages.getInstance().aheadBehindStatus(aheadBehind);
        }
    }
}
