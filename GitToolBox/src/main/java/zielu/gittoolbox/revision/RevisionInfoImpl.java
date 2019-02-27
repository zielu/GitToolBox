package zielu.gittoolbox.revision;

import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class RevisionInfoImpl implements RevisionInfo {
  private final VcsRevisionNumber revisionNumber;
  private final String author;
  private final LocalDateTime date;
  private final String subject;
  private final String details;

  RevisionInfoImpl(@NotNull VcsRevisionNumber revisionNumber, String author, Date revisionDate, String subject,
                   String details) {
    this.revisionNumber = revisionNumber;
    this.author = author != null ? author.trim() : "EMPTY";
    this.date = convertDate(revisionDate);
    this.subject = subject;
    this.details = details;
  }

  @NotNull
  private LocalDateTime convertDate(@Nullable Date date) {
    if (date != null) {
      return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    } else {
      return LocalDateTime.now();
    }
  }

  @NotNull
  @Override
  public VcsRevisionNumber getRevisionNumber() {
    return revisionNumber;
  }

  @NotNull
  @Override
  public String getAuthor() {
    return author;
  }

  @NotNull
  @Override
  public LocalDateTime getDate() {
    return date;
  }

  @Nullable
  @Override
  public String getSubject() {
    return subject;
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
