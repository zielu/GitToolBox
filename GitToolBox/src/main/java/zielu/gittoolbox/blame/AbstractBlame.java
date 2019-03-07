package zielu.gittoolbox.blame;

import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import java.util.Date;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class AbstractBlame implements Blame {
  private final VcsRevisionNumber revisionNumber;
  private final String author;
  private final Date date;

  AbstractBlame(@NotNull VcsRevisionNumber revisionNumber, @Nullable String author, @NotNull Date date) {
    this.revisionNumber = revisionNumber;
    this.author = author;
    this.date = date;
  }

  @Nullable
  @Override
  public String getAuthor() {
    return author;
  }

  @NotNull
  @Override
  public Date getDate() {
    return date;
  }

  @NotNull
  @Override
  public VcsRevisionNumber getRevisionNumber() {
    return revisionNumber;
  }
}
