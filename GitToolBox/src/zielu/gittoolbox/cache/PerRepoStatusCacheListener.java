package zielu.gittoolbox.cache;

import com.google.common.collect.ImmutableMap;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;

public interface PerRepoStatusCacheListener {
    void initialized(ImmutableMap<GitRepository, RepoInfo> info);
    void stateChanged(@NotNull RepoInfo info,  @NotNull GitRepository repository);
}
