package zielu.gittoolbox.revision;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

public interface RevisionDataProvider {
  @Nullable
  Date getDate(int lineIndex);

  @Nullable
  String getAuthor(int lineIndex);

  @Nullable
  String getEmail(int lineIndex);

  @Nullable
  String getSubject(int lineIndex);

  @Nullable
  VcsRevisionNumber getRevisionNumber(int lineIndex);

  @Nullable
  VcsRevisionNumber getCurrentRevisionNumber();

  @NotNull
  Project getProject();

  @Nullable
  VirtualFile getFile();

  int getLineCount();
}
