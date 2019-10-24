package zielu.gittoolbox.revision;

import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import java.util.Date;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.util.GtStringUtil;

class LightRevisionInfoFactory implements RevisionInfoFactory {
  @NotNull
  @Override
  public RevisionInfo forLine(@NotNull RevisionDataProvider provider, int lineNumber) {
    Date lineDate = prepareDate(provider.getDate(lineNumber));
    String author = prepareAuthor(provider.getAuthor(lineNumber));
    String subject = provider.getSubject(lineNumber);
    VcsRevisionNumber revisionNumber = provider.getRevisionNumber(lineNumber);
    return new RevisionInfoImpl(ObjectUtils.defaultIfNull(revisionNumber, VcsRevisionNumber.NULL),
        author, lineDate, subject);
  }

  @NotNull
  @Override
  public RevisionInfo forFile(@NotNull VirtualFile file, @NotNull VcsFileRevision revision) {
    Date date = prepareDate(revision.getRevisionDate());
    String author = prepareAuthor(revision.getAuthor());
    String commitMessage = revision.getCommitMessage();
    String subject = GtStringUtil.firstLine(commitMessage);
    return new RevisionInfoImpl(revision.getRevisionNumber(), author, date, subject);
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
  private Date prepareDate(Date revisionDate) {
    if (revisionDate != null) {
      return revisionDate;
    } else {
      return new Date();
    }
  }
}
