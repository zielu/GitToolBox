package zielu.gittoolbox.revision;

import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.impl.NullVirtualFile;
import java.time.ZonedDateTime;
import org.jetbrains.annotations.Nullable;

public interface RevisionDataProvider {
  RevisionDataProvider EMPTY = new RevisionDataProvider() {
    @Nullable
    @Override
    public ZonedDateTime getAuthorDateTime(int lineIndex) {
      return null;
    }

    @Nullable
    @Override
    public String getAuthor(int lineIndex) {
      return null;
    }

    @Nullable
    @Override
    public String getAuthorEmail(int lineIndex) {
      return null;
    }

    @Nullable
    @Override
    public String getSubject(int lineIndex) {
      return null;
    }

    @Override
    public VcsRevisionNumber getRevisionNumber(int lineIndex) {
      return VcsRevisionNumber.NULL;
    }

    @Override
    public VcsRevisionNumber getBaseRevision() {
      return VcsRevisionNumber.NULL;
    }

    @Override
    public VirtualFile getFile() {
      return NullVirtualFile.INSTANCE;
    }

    @Override
    public int getLineCount() {
      return 0;
    }

    @Override
    public String toString() {
      return "RevisionDataProvider[EMPTY]";
    }
  };

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
