package zielu.gittoolbox.cache;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.Topic;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface VirtualFileRepoCache extends DirMappingAware {
  Topic<VirtualFileCacheListener> CACHE_CHANGE = Topic.create("File cache change",
      VirtualFileCacheListener.class);

  @NotNull
  @SuppressFBWarnings({"NP_NULL_ON_SOME_PATH", "NP_NONNULL_RETURN_VIOLATION"})
  static VirtualFileRepoCache getInstance(@NotNull Project project) {
    return project.getComponent(VirtualFileRepoCache.class);
  }

  @Nullable
  GitRepository getRepoForRoot(@NotNull VirtualFile root);

  @Nullable
  GitRepository getRepoForDir(@NotNull VirtualFile dir);
}
