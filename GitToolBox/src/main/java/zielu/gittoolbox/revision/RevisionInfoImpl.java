package zielu.gittoolbox.revision;

import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import java.time.ZonedDateTime;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class RevisionInfoImpl implements RevisionInfo {
  private final VcsRevisionNumber revisionNumber;
  private final String author;
  private final String authorEmail;
  private final ZonedDateTime date;
  private final String subject;

  RevisionInfoImpl(@NotNull VcsRevisionNumber revisionNumber, @NotNull String author,
                   @NotNull ZonedDateTime revisionDate, String authorEmail,
                   String subject) {
    this.revisionNumber = revisionNumber;
    this.author = author != null ? prepareAuthor(author) : "EMPTY";
    this.authorEmail = authorEmail;
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

  @Nullable
  @Override
  public String getAuthorEmail() {
    return authorEmail;
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
