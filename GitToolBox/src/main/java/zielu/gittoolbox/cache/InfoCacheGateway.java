package zielu.gittoolbox.cache;

import static zielu.gittoolbox.cache.PerRepoInfoCache.CACHE_CHANGE;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import git4idea.repo.GitRepository;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;

class InfoCacheGateway {
  private final Logger log = Logger.getInstance(getClass());
  private final Project project;

  private final MessageBus messageBus;

  InfoCacheGateway(@NotNull Project project) {
    this.project = project;
    messageBus = project.getMessageBus();
  }

  void notifyEvicted(@NotNull Collection<GitRepository> repositories) {
    messageBus.syncPublisher(CACHE_CHANGE).evicted(repositories);
  }

  void notifyRepoChanged(GitRepository repo, RepoInfo info) {
    messageBus.syncPublisher(CACHE_CHANGE).stateChanged(info, repo);
    log.debug("Published cache changed event: ", repo);
  }
}
