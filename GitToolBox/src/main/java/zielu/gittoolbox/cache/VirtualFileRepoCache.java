package zielu.gittoolbox.cache;

import com.google.common.base.Preconditions;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.Topic;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.util.AppUtil;

public interface VirtualFileRepoCache extends DirMappingAware {
  Topic<VirtualFileCacheListener> CACHE_CHANGE = Topic.create("File cache change",
      VirtualFileCacheListener.class);

  @NotNull
  static VirtualFileRepoCache getInstance(@NotNull Project project) {
    return AppUtil.getServiceInstance(project, VirtualFileRepoCache.class);
  }

  @Nullable
  GitRepository getRepoForRoot(@NotNull VirtualFile root);

  @Nullable
  GitRepository getRepoForDir(@NotNull VirtualFile dir);

  @Nullable
  GitRepository getRepoForPath(@NotNull FilePath path);

  @Nullable
  default GitRepository getRepoForFile(@NotNull VirtualFile file) {
    Preconditions.checkArgument(!file.isDirectory(), "%s is not file", file);
    VirtualFile parent = file.getParent();
    if (parent != null && parent.isDirectory()) {
      return getRepoForDir(parent);
    }
    return null;
  }

  @Nullable
  default VirtualFile getRepoRootForFile(@NotNull VirtualFile file) {
    GitRepository repo = getRepoForFile(file);
    if (repo != null) {
      return repo.getRoot();
    }
    return null;
  }

  default boolean isUnderGitRoot(@NotNull VirtualFile file) {
    return (file.isDirectory() ? getRepoForDir(file) : getRepoForFile(file)) != null;
  }

  default boolean isUnderGitRoot(@NotNull FilePath path) {
    return getRepoForPath(path) != null;
  }
}
