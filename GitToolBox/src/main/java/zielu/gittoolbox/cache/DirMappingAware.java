package zielu.gittoolbox.cache;

import com.google.common.collect.ImmutableList;
import git4idea.repo.GitRepository;

public interface DirMappingAware {
  void updatedRepoList(ImmutableList<GitRepository> repositories);
}
