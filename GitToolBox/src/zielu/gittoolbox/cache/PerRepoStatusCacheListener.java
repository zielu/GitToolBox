package zielu.gittoolbox.cache;

import com.google.common.base.Optional;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.status.GitAheadBehindCount;

public interface PerRepoStatusCacheListener {
    void stateChanged(@NotNull Optional<GitAheadBehindCount> aheadBehind,  @NotNull GitRepository repository);
}
