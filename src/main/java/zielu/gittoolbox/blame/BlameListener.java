package zielu.gittoolbox.blame;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public interface BlameListener {
  default void blameUpdated(@NotNull VirtualFile file) {
  }

  default void blameInvalidated(@NotNull VirtualFile file) {
  }
}
