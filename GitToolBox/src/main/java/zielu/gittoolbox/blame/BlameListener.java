package zielu.gittoolbox.blame;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public interface BlameListener {
  default void blameUpdated(@NotNull VirtualFile file) {
  }

  default void blameUpdated(@NotNull Editor editor, @NotNull VirtualFile file) {
  }
}
