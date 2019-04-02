package zielu.gittoolbox.revision;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import java.util.Date;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface RevisionDataProvider {
  @Nullable
  Date getDate(int lineNumber);

  @Nullable
  String getAuthor(int lineNumber);

  @Nullable
  String getSubject(int lineNumber);

  @Nullable
  String getMessage(int lineNumber);

  @Nullable
  VcsRevisionNumber getRevisionNumber(int lineNumber);

  @Nullable
  VcsRevisionNumber getCurrentRevisionNumber();

  @NotNull
  Project getProject();

  @Nullable
  VirtualFile getFile();

  int getLineCount();
}
