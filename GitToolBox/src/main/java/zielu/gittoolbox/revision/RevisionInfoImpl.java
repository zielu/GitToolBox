package zielu.gittoolbox.revision;

import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import java.time.ZonedDateTime;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class RevisionInfoImpl implements RevisionInfo {
  private final VcsRevisionNumber revisionNumber;
  private final String author;
  private final ZonedDateTime date;
  private final String subject;

  RevisionInfoImpl(@NotNull VcsRevisionNumber revisionNumber, String author, ZonedDateTime revisionDate,
                   String subject) {
    this.revisionNumber = revisionNumber;
    this.author = author != null ? prepareAuthor(author) : "EMPTY";
    this.date = revisionDate;
    this.subject = subject;
  }

  @NotNull
  private String prepareAuthor(@NotNull String author) {
    return author.trim().replaceAll("\\(.*\\)", "");
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
  public ZonedDateTime getDate() {
    return date;
  }

  @Nullable
  @Override
  public String getSubject() {
    return subject;
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
