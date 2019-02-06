package zielu.gittoolbox.blame;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

class BlameCacheGateway {
  private final Project project;

  BlameCacheGateway(@NotNull Project project) {
    this.project = project;
  }

  void fireBlameUpdated(@NotNull VirtualFile file, @NotNull BlameAnnotation annotation) {
    project.getMessageBus().syncPublisher(BlameCache.CACHE_UPDATES).cacheUpdated(file, annotation);
  }

  void fireBlameInvalidated(@NotNull VirtualFile file) {
    project.getMessageBus().syncPublisher(BlameCache.CACHE_UPDATES).invalidated(file);
  }
}
