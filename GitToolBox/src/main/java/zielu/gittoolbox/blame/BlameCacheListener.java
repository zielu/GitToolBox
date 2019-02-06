package zielu.gittoolbox.blame;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public interface BlameCacheListener {
  void cacheUpdated(@NotNull VirtualFile file, @NotNull BlameAnnotation annotation);
  void invalidated(@NotNull VirtualFile file);
}
