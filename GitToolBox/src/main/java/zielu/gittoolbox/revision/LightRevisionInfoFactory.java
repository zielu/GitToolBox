package zielu.gittoolbox.revision;

import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.util.GtStringUtil;

import java.util.Date;

class LightRevisionInfoFactory implements RevisionInfoFactory {
  @NotNull
  @Override
  public RevisionInfo forLine(@NotNull RevisionDataProvider provider, int lineNumber) {
    Date lineDate = provider.getDate(lineNumber);
    String author = provider.getAuthor(lineNumber);
    String email = provider.getEmail(lineNumber);
    String subject = provider.getSubject(lineNumber);
    return new RevisionInfoImpl(provider.getRevisionNumber(lineNumber), author, email, lineDate, subject);
  }

  @NotNull
  @Override
  public RevisionInfo forFile(@NotNull VirtualFile file, @NotNull VcsFileRevision revision) {
    Date date = revision.getRevisionDate();
    String author = revision.getAuthor();
    String commitMessage = revision.getCommitMessage();
    String subject = GtStringUtil.firstLine(commitMessage);
    return new RevisionInfoImpl(revision.getRevisionNumber(), author, author, date, subject);
  }
}
