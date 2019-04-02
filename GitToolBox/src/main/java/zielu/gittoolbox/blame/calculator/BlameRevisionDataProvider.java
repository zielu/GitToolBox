package zielu.gittoolbox.blame.calculator;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import java.util.Date;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.revision.RevisionDataProvider;

class BlameRevisionDataProvider implements RevisionDataProvider {
  private final Project project;
  private final List<CommitInfo> lineInfos;
  private final VirtualFile file;
  private final VcsRevisionNumber baseRevision;

  BlameRevisionDataProvider(@NotNull Project project, @NotNull List<CommitInfo> lineInfos,
                            @Nullable VirtualFile file, @Nullable VcsRevisionNumber baseRevision) {
    this.project = project;
    this.lineInfos = lineInfos;
    this.file = file;
    this.baseRevision = baseRevision;
  }

  @Nullable
  @Override
  public Date getDate(int lineNumber) {
    return getLineCommit(lineNumber).getAuthorDate();
  }

  private CommitInfo getLineCommit(int lineNumber) {
    return lineInfos.get(lineToIndex(lineNumber));
  }

  private int lineToIndex(int lineNumber) {
    return lineNumber - 1;
  }

  @Nullable
  @Override
  public String getAuthor(int lineNumber) {
    return getLineCommit(lineNumber).getAuthorName();
  }

  @Nullable
  @Override
  public String getSubject(int lineNumber) {
    return getLineCommit(lineNumber).getSummary();
  }

  @Nullable
  @Override
  public String getMessage(int lineNumber) {
    return null;
  }

  @Nullable
  @Override
  public VcsRevisionNumber getRevisionNumber(int lineNumber) {
    return getLineCommit(lineNumber).getRevisionNumber();
  }

  @Nullable
  @Override
  public VcsRevisionNumber getCurrentRevisionNumber() {
    return baseRevision;
  }

  @NotNull
  @Override
  public Project getProject() {
    return project;
  }

  @Nullable
  @Override
  public VirtualFile getFile() {
    return file;
  }

  @Override
  public int getLineCount() {
    return lineInfos.size() + 1;
  }
}
