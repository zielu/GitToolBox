package zielu.gittoolbox.blame;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBusConnection;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.cache.PerRepoInfoCache;
import zielu.gittoolbox.cache.PerRepoStatusCacheListener;
import zielu.gittoolbox.cache.RepoInfo;

class BlameSubscriber {

  BlameSubscriber(@NotNull Project project) {
    MessageBusConnection connection = project.getMessageBus().connect(project);
    connection.subscribe(PerRepoInfoCache.CACHE_CHANGE, new PerRepoStatusCacheListener() {
      @Override
      public void stateChanged(@NotNull RepoInfo info, @NotNull GitRepository repository) {
        BlameCache.getExistingInstance(project).ifPresent(cache -> cache.refreshForRoot(repository.getRoot()));
      }
    });
    connection.subscribe(BlameCache.CACHE_UPDATES, new BlameCacheListener() {
      @Override
      public void cacheUpdated(@NotNull VirtualFile file, @NotNull BlameAnnotation annotation) {
        BlameService.getExistingInstance(project).ifPresent(service -> service.blameUpdated(file, annotation));
      }

      @Override
      public void invalidated(@NotNull VirtualFile file) {
        BlameService.getExistingInstance(project).ifPresent(service -> service.invalidate(file));
      }
    });
  }
}
