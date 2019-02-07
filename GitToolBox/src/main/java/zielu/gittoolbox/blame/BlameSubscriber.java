package zielu.gittoolbox.blame;

import com.intellij.openapi.components.BaseComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBusConnection;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.cache.PerRepoInfoCache;
import zielu.gittoolbox.cache.PerRepoStatusCacheListener;
import zielu.gittoolbox.cache.RepoInfo;

class BlameSubscriber implements BaseComponent {
  private MessageBusConnection connection;

  BlameSubscriber(@NotNull Project project, @NotNull BlameCache blameCache, @NotNull BlameService blameService) {
    connection = project.getMessageBus().connect();
    connection.subscribe(PerRepoInfoCache.CACHE_CHANGE, new PerRepoStatusCacheListener() {
      @Override
      public void stateChanged(@NotNull RepoInfo info, @NotNull GitRepository repository) {
        blameCache.refreshForRoot(repository.getRoot());
      }
    });
    connection.subscribe(BlameCache.CACHE_UPDATES, new BlameCacheListener() {
      @Override
      public void cacheUpdated(@NotNull VirtualFile file, @NotNull BlameAnnotation annotation) {
        blameService.blameUpdated(file, annotation);
      }

      @Override
      public void invalidated(@NotNull VirtualFile file) {
        blameService.invalidate(file);
      }
    });
  }

  @Override
  public void disposeComponent() {
    if (connection != null) {
      connection.disconnect();
      connection = null;
    }
  }
}
