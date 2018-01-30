package zielu.gittoolbox.cache;

import com.intellij.openapi.project.Project;
import com.intellij.util.messages.Topic;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;

public interface PerRepoInfoCache extends DirMappingAware, RepoChangeAware {
  Topic<PerRepoStatusCacheListener> CACHE_CHANGE = Topic.create("Status cache change",
      PerRepoStatusCacheListener.class);
  @NotNull
  RepoInfo getInfo(GitRepository repository);

  void refreshAll();

  void refresh(Iterable<GitRepository> repositories);

  @NotNull
  static PerRepoInfoCache getInstance(@NotNull Project project) {
    return project.getComponent(PerRepoInfoCache.class);
  }
}
