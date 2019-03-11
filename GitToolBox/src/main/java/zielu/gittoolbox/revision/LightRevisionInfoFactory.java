package zielu.gittoolbox.revision;

import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.annotate.LineAnnotationAspect;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.annotate.GitFileAnnotation;
import java.util.Date;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.util.GtStringUtil;

class LightRevisionInfoFactory implements RevisionInfoFactory {
  @NotNull
  @Override
  public RevisionInfo forLine(@NotNull FileAnnotation annotation, @NotNull VcsRevisionNumber lineRevision,
                              int lineNumber) {
    Date lineDate = annotation.getLineDate(lineNumber);
    String author = null;
    for (LineAnnotationAspect aspect : annotation.getAspects()) {
      if (LineAnnotationAspect.AUTHOR.equals(aspect.getId())) {
        author = aspect.getValue(lineNumber);
      }
    }
    GitFileAnnotation gitAnnotation = (GitFileAnnotation) annotation;
    String commitMessage = gitAnnotation.getCommitMessage(lineRevision);
    String subject = extractSubject(commitMessage);
    return new RevisionInfoImpl(lineRevision, author, lineDate, subject, commitMessage);
  }

  @Nullable
  private String extractSubject(@Nullable String commitMessage) {
    return GtStringUtil.firstLine(commitMessage);
  }

  @NotNull
  @Override
  public RevisionInfo forFile(@NotNull VirtualFile file, @NotNull VcsFileRevision revision) {
    Date date = revision.getRevisionDate();
    String author = revision.getAuthor();
    String commitMessage = revision.getCommitMessage();
    String subject = extractSubject(commitMessage);
    return new RevisionInfoImpl(revision.getRevisionNumber(), author, date, subject, commitMessage);
  }
}
