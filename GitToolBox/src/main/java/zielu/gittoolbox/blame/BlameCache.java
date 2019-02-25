package zielu.gittoolbox.blame;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.Topic;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.util.AppUtil;

public interface BlameCache {
  Topic<BlameCacheListener> CACHE_UPDATES = Topic.create("blame cache updates", BlameCacheListener.class);

  @NotNull
  BlameAnnotation getAnnotation(@NotNull VirtualFile file);

  void refreshForRoot(@NotNull VirtualFile root);

  void invalidate(@NotNull VirtualFile file);

  @NotNull
  static Optional<BlameCache> getExistingInstance(@NotNull Project project) {
    return AppUtil.getExistingServiceInstance(project, BlameCache.class);
  }
}
