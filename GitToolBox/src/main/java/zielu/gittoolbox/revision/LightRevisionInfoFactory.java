package zielu.gittoolbox.revision;

import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import java.time.ZonedDateTime;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;

class LightRevisionInfoFactory implements RevisionInfoFactory {
  @NotNull
  @Override
  public RevisionInfo forLine(@NotNull RevisionDataProvider provider, int lineNumber) {
    ZonedDateTime authorDateTime = prepareDate(provider.getAuthorDateTime(lineNumber));
    String author = prepareAuthor(provider.getAuthor(lineNumber));
    String authorEmail = provider.getAuthorEmail(lineNumber);
    String subject = provider.getSubject(lineNumber);
    VcsRevisionNumber revisionNumber = provider.getRevisionNumber(lineNumber);
    return new RevisionInfoImpl(ObjectUtils.defaultIfNull(revisionNumber, VcsRevisionNumber.NULL),
        author, authorDateTime, subject, authorEmail);
  }

  @NotNull
  private String prepareAuthor(String author) {
    if (author == null) {
      return "EMPTY";
    } else {
      return author.trim().replaceAll("\\(.*\\)", "");
    }
  }

  @NotNull
  private ZonedDateTime prepareDate(ZonedDateTime revisionDate) {
    if (revisionDate != null) {
      return revisionDate;
    } else {
      return ZonedDateTime.now();
    }
  }
}
