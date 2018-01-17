package zielu.gittoolbox.cache;

import com.intellij.openapi.project.Project;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;

public interface PerRepoInfoCache extends DirMappingAware, RepoChangeAware {
  @NotNull
  RepoInfo getInfo(GitRepository repository);

  void refreshAll();

  void refresh(Iterable<GitRepository> repositories);

  @NotNull
  @SuppressFBWarnings({"NP_NULL_ON_SOME_PATH", "NP_NONNULL_RETURN_VIOLATION"})
  static PerRepoInfoCache getInstance(@NotNull Project project) {
    return project.getComponent(PerRepoInfoCache.class);
  }
}
