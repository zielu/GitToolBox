package zielu.gittoolbox.blame;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBus;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.cache.VirtualFileRepoCache;
import zielu.gittoolbox.util.GatewayBase;

class BlameCacheGateway extends GatewayBase {
  private final BlameFactory blameFactory;
  private final MessageBus messageBus;

  BlameCacheGateway(@NotNull Project project, @NotNull VirtualFileRepoCache repoCache) {
    super(project);
    blameFactory = new BlameFactory(project, repoCache);
    messageBus = project.getMessageBus();
  }

  void fireBlameUpdated(@NotNull VirtualFile file, @NotNull BlameAnnotation annotation) {
    messageBus.syncPublisher(BlameCache.CACHE_UPDATES).cacheUpdated(file, annotation);
  }

  void fireBlameInvalidated(@NotNull VirtualFile file) {
    messageBus.syncPublisher(BlameCache.CACHE_UPDATES).invalidated(file);
  }

  @NotNull
  BlameFactory blameFactory() {
    return blameFactory;
  }
}
