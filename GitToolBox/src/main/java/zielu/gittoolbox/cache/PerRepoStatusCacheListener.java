package zielu.gittoolbox.cache;

import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;

public interface PerRepoStatusCacheListener {
    default void stateChanged(@NotNull RepoInfo info, @NotNull GitRepository repository) {
    }

    default void stateRefreshed(@NotNull RepoInfo info, @NotNull GitRepository repository) {
    }
}
