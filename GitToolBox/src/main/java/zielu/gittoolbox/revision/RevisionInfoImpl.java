package zielu.gittoolbox.revision;

import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import java.util.Date;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class RevisionInfoImpl implements RevisionInfo {
  private final VcsRevisionNumber revisionNumber;
  private final String author;
  private final Date date;
  private final String subject;

  RevisionInfoImpl(@NotNull VcsRevisionNumber revisionNumber, String author, @Nullable Date revisionDate,
                   String subject) {
    this.revisionNumber = revisionNumber;
    this.author = author != null ? prepareAuthor(author) : "EMPTY";
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
