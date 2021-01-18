package zielu.gittoolbox.cache;

import com.google.common.base.Preconditions;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.Topic;
import git4idea.repo.GitRepository;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.util.AppUtil;
import zielu.gittoolbox.util.GtUtil;

public interface VirtualFileRepoCache extends DirMappingAware {
  Topic<VirtualFileRepoCacheListener> CACHE_CHANGE = Topic.create("File cache change",
      VirtualFileRepoCacheListener.class);

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

  boolean hasAnyRepositories();

  List<GitRepository> getRepositories();

  default Optional<GitRepository> findRepoForRoot(@NotNull String rootPath) {
    return Optional.of(rootPath)
            .map(GtUtil::findFileByUrl)
            .filter(Objects::nonNull)
            .map(this::getRepoForRoot);
  }

  default List<GitRepository> findReposForRoots(@NotNull Collection<String> rootPaths) {
    return rootPaths.stream()
            .map(GtUtil::findFileByUrl)
            .filter(Objects::nonNull)
            .map(this::getRepoForRoot)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
  }

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

  default Optional<GitRepository> findRepoForFileOrDir(@NotNull VirtualFile fileOrDir) {
    return Optional.ofNullable(fileOrDir.isDirectory() ? getRepoForDir(fileOrDir) : getRepoForFile(fileOrDir));
  }

  default boolean isUnderGitRoot(@NotNull VirtualFile file) {
    return (file.isDirectory() ? getRepoForDir(file) : getRepoForFile(file)) != null;
  }
}
