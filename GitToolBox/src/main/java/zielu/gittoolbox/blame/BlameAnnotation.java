package zielu.gittoolbox.blame;

import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BlameAnnotation {
  @NotNull
  Blame getBlame(int lineNumber);

  boolean isChanged(@NotNull VcsRevisionNumber revision);

  @Nullable
  VirtualFile getVirtualFile();
}
