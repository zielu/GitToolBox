package zielu.gittoolbox.cache;

import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;

public interface PerRepoStatusCacheListener {
    void stateChanged(@NotNull GitRepository repository);
}
