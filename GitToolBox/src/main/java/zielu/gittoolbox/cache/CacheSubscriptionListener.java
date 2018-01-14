package zielu.gittoolbox.cache;

import com.google.common.collect.ImmutableList;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;

public interface CacheSubscriptionListener {
  default void repoChanged(@NotNull GitRepository repository) {
  }

  default void dirMappingChanged(ImmutableList<GitRepository> repositories) {
  }
}
