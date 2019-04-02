package zielu.gittoolbox.blame;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.annotate.LineAnnotationAspect;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.annotate.GitFileAnnotation;
import java.util.Date;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.revision.RevisionDataProvider;
import zielu.gittoolbox.util.GtStringUtil;

class FileAnnotationRevisionDataProvider implements RevisionDataProvider {
  private final FileAnnotation annotation;

  FileAnnotationRevisionDataProvider(@NotNull FileAnnotation annotation) {
    this.annotation = annotation;
  }

  @Nullable
  @Override
  public Date getDate(int lineNumber) {
    return annotation.getLineDate(lineNumber);
  }

  @Nullable
  @Override
  public String getAuthor(int lineNumber) {
    for (LineAnnotationAspect aspect : annotation.getAspects()) {
      if (LineAnnotationAspect.AUTHOR.equals(aspect.getId())) {
        return aspect.getValue(lineNumber);
      }
    }
    return null;
  }

  @Nullable
  @Override
  public String getSubject(int lineNumber) {
    String message = getMessage(lineNumber);
    return GtStringUtil.firstLine(message);
  }

  @Nullable
  @Override
  public String getMessage(int lineNumber) {
    GitFileAnnotation gitAnnotation = (GitFileAnnotation) annotation;
    VcsRevisionNumber revisionNumber = annotation.getLineRevisionNumber(lineNumber);
    if (revisionNumber != null) {
      return gitAnnotation.getCommitMessage(revisionNumber);
    }
    return null;
  }

  @Nullable
  @Override
  public VcsRevisionNumber getRevisionNumber(int lineNumber) {
    return annotation.getLineRevisionNumber(lineNumber);
  }

  @Nullable
  @Override
  public VcsRevisionNumber getCurrentRevisionNumber() {
    return annotation.getCurrentRevision();
  }

  @NotNull
  @Override
  public Project getProject() {
    return annotation.getProject();
  }

  @Nullable
  @Override
  public VirtualFile getFile() {
    return annotation.getFile();
  }

  @Override
  public int getLineCount() {
    return annotation.getLineCount();
  }
}
