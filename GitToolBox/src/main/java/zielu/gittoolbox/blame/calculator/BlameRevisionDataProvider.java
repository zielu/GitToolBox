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
  public Date getDate(int lineIndex) {
    return getLineCommit(lineIndex).getAuthorDate();
  }

  private CommitInfo getLineCommit(int lineIndex) {
    return lineInfos.get(lineIndex);
  }

  @Nullable
  @Override
  public String getAuthor(int lineIndex) {
    return getLineCommit(lineIndex).getAuthorName();
  }

  @Nullable
  @Override
  public String getSubject(int lineIndex) {
    return getLineCommit(lineIndex).getSummary();
  }

  @Nullable
  @Override
  public VcsRevisionNumber getRevisionNumber(int lineIndex) {
    return getLineCommit(lineIndex).getRevisionNumber();
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
    return lineInfos.size();
  }
}
