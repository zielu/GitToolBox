package zielu.gittoolbox.revision;

import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import java.util.Date;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.util.GtStringUtil;

class LightRevisionInfoFactory implements RevisionInfoFactory {
  @NotNull
  @Override
  public RevisionInfo forLine(@NotNull RevisionDataProvider provider, @NotNull VcsRevisionNumber lineRevision,
                              int lineNumber) {
    Date lineDate = provider.getDate(lineNumber);
    String author = provider.getAuthor(lineNumber);
    String subject = provider.getSubject(lineNumber);
    return new RevisionInfoImpl(lineRevision, author, lineDate, subject);
  }

  @NotNull
  @Override
  public RevisionInfo forFile(@NotNull VirtualFile file, @NotNull VcsFileRevision revision) {
    Date date = revision.getRevisionDate();
    String author = revision.getAuthor();
    String commitMessage = revision.getCommitMessage();
    String subject = GtStringUtil.firstLine(commitMessage);
    return new RevisionInfoImpl(revision.getRevisionNumber(), author, date, subject);
  }
}
