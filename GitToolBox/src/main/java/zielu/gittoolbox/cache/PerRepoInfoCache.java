package zielu.gittoolbox.cache;

import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;

public interface PerRepoInfoCache {
  @NotNull
  RepoInfo getInfo(GitRepository repository);

  void refreshAll();

  void refresh(Iterable<GitRepository> repositories);
}
