package zielu.gittoolbox.blame;

import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class AbstractBlame implements Blame {
  private final VcsRevisionNumber revisionNumber;

  AbstractBlame(@NotNull VcsRevisionNumber revisionNumber) {
    this.revisionNumber = revisionNumber;
  }

  @NotNull
  protected LocalDate convertDate(@Nullable Date date) {
    if (date != null) {
      return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    } else {
      return LocalDate.now();
    }
  }

  @NotNull
  @Override
  public VcsRevisionNumber getRevisionNumber() {
    return revisionNumber;
  }
}
