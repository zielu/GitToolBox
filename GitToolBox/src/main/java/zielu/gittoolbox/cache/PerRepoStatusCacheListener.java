package zielu.gittoolbox.cache;

import git4idea.repo.GitRepository;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;

public interface PerRepoStatusCacheListener {
  default void stateChanged(@NotNull RepoInfo info, @NotNull GitRepository repository) {
  }

  default void evicted(@NotNull Collection<GitRepository> repositories) {
  }
}
