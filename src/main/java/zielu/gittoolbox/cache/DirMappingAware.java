package zielu.gittoolbox.cache;

import git4idea.repo.GitRepository;
import java.util.List;

interface DirMappingAware {
  void updatedRepoList(List<GitRepository> repositories);
}
