package zielu.gittoolbox.revision;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import java.time.ZonedDateTime;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface RevisionDataProvider {
  @Nullable
  ZonedDateTime getAuthorDateTime(int lineIndex);

  @Nullable
  String getAuthor(int lineIndex);

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
