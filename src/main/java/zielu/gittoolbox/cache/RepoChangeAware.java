package zielu.gittoolbox.cache;

import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;

interface RepoChangeAware {
  void repoChanged(@NotNull GitRepository repository);
}
