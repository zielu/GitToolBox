package zielu.gittoolbox.blame;

import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import java.time.LocalDate;
import java.util.Date;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class BlameImpl extends AbstractBlame {
  private final String author;
  private final LocalDate date;
  private final String details;

  BlameImpl(@NotNull VcsRevisionNumber revisionNumber, String author, Date revisionDate, String details) {
    super(revisionNumber);
    this.author = author != null ? author.trim() : "EMPTY";
    this.date = convertDate(revisionDate);
    this.details = details;
  }

  @NotNull
  @Override
  public String getAuthor() {
    return author;
  }

  @NotNull
  @Override
  public LocalDate getDate() {
    return date;
  }

  @Nullable
  @Override
  public String getDetails() {
    return details;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public boolean isNotEmpty() {
    return true;
  }
}
