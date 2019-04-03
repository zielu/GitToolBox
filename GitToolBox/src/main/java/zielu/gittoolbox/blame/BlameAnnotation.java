package zielu.gittoolbox.blame;

import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.revision.RevisionInfo;

public interface BlameAnnotation {
  BlameAnnotation EMPTY = new BlameAnnotation() {
    @NotNull
    @Override
    public RevisionInfo getBlame(int lineIndex) {
      return RevisionInfo.EMPTY;
    }

    @Override
    public boolean isChanged(@NotNull VcsRevisionNumber revision) {
      return !VcsRevisionNumber.NULL.equals(revision);
    }

    @Override
    public boolean updateRevision(@NotNull RevisionInfo revisionInfo) {
      return false;
    }

    @Nullable
    @Override
    public VirtualFile getVirtualFile() {
      return null;
    }

    @Override
    public String toString() {
      return "BlameAnnotation:EMPTY";
    }
  };

  @NotNull
  RevisionInfo getBlame(int lineIndex);

  boolean isChanged(@NotNull VcsRevisionNumber revision);

  boolean updateRevision(@NotNull RevisionInfo revisionInfo);

  @Nullable
  VirtualFile getVirtualFile();
}
