package zielu.gittoolbox.revision;

import java.time.ZonedDateTime;
import org.jetbrains.annotations.NotNull;

class LightRevisionInfoFactory implements RevisionInfoFactory {
  @NotNull
  @Override
  public RevisionInfo forLine(@NotNull RevisionDataProvider provider, int lineNumber) {
    ZonedDateTime authorDateTime = provider.getAuthorDateTime(lineNumber);
    if (authorDateTime == null) {
      authorDateTime = ZonedDateTime.now();
    }
    String author = provider.getAuthor(lineNumber);
    String subject = provider.getSubject(lineNumber);
    String authorEmail = provider.getAuthorEmail(lineNumber);
    return new RevisionInfoImpl(provider.getRevisionNumber(lineNumber), author, authorDateTime, authorEmail, subject);
  }
}
