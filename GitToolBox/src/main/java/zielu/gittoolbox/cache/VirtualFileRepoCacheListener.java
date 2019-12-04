package zielu.gittoolbox.cache;

import com.intellij.openapi.vfs.VirtualFile;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;

/**
 * Notification interface.
 * <p/>
 * Notification methods are called in following order:
 * <ol>
 *   <li>{@link #removed(Collection)}</li>
 *   <li>{@link #added(Collection)}</li>
 *   <li>{@link #updated()}</li>
 * </ol>
 */
public interface VirtualFileRepoCacheListener {
  default void updated() {
  }

  default void added(@NotNull Collection<VirtualFile> roots) {
  }

  default void removed(@NotNull Collection<VirtualFile> roots) {
  }
}
