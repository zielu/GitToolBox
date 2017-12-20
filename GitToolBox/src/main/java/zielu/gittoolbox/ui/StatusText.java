package zielu.gittoolbox.ui;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.status.GitAheadBehindCount;
import zielu.gittoolbox.status.Status;

public final class StatusText {
  private StatusText() {
    throw new IllegalStateException();
  }

  @SuppressFBWarnings({"NP_NULL_ON_SOME_PATH"})
  public static String format(@NotNull GitAheadBehindCount aheadBehind) {
    Status status = aheadBehind.status();
    if (status.isValid()) {
      if (status == Status.NO_REMOTE) {
        return ResBundle.getString("git.no.remote");
      } else {
        return StatusMessages.getInstance().aheadBehindStatus(aheadBehind);
      }
    } else {
      return ResBundle.getString("git.na");
    }
  }

  @SuppressFBWarnings({"NP_NULL_ON_SOME_PATH"})
  public static String formatToolTip(@NotNull GitAheadBehindCount aheadBehind) {
    if (aheadBehind.status() == Status.SUCCESS) {
      return "";
    } else {
      return StatusMessages.getInstance().aheadBehindStatus(aheadBehind);
    }
  }
}
