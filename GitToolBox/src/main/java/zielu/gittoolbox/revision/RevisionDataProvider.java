package zielu.gittoolbox.revision;

import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import java.time.ZonedDateTime;
import org.jetbrains.annotations.Nullable;

public interface RevisionDataProvider {
  @Nullable
  ZonedDateTime getAuthorDateTime(int lineIndex);

  @Nullable
  String getAuthor(int lineIndex);

  @Nullable
  String getAuthorEmail(int lineIndex);

  @Nullable
  String getSubject(int lineIndex);

  VcsRevisionNumber getRevisionNumber(int lineIndex);

  VcsRevisionNumber getBaseRevision();

  VirtualFile getFile();

  int getLineCount();
}
