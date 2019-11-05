package zielu.gittoolbox.revision;

import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

final class RevisionInfoImpl implements RevisionInfo {
  private final VcsRevisionNumber revisionNumber;
  private final String author;
  private final String email;
  private final Date date;
  private final String subject;

  RevisionInfoImpl(@NotNull VcsRevisionNumber revisionNumber, String author, String email,
                   @Nullable Date revisionDate, String subject) {
    this.revisionNumber = revisionNumber;
    this.author = author != null ? prepareAuthor(author) : "EMPTY";
    this.email = email != null ? prepareEmail(email) : "EMPTY";
    if (revisionDate != null) {
      date = revisionDate;
    } else {
      date = new Date();
    }
    this.subject = subject;
  }

  @NotNull
  private String prepareAuthor(@NotNull String author) {
    return author.trim().replaceAll("\\(.*\\)", "");
  }

  @NotNull
  private String prepareEmail(@NotNull String email) {
    email = email.trim();
    return email.substring(1, email.length() - 1);
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
  public String getEmail() {
    return email;
  }

  @NotNull
  @Override
  public Date getDate() {
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
