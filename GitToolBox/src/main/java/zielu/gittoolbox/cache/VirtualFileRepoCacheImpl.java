package zielu.gittoolbox.cache;

import static java.util.function.Function.identity;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.repo.GitRepository;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VirtualFileRepoCacheImpl implements VirtualFileRepoCache, ProjectComponent {
  private final Logger log = Logger.getInstance(getClass());
  private final ConcurrentMap<VirtualFile, GitRepository> rootsCache = new ConcurrentHashMap<>();
  private final ConcurrentMap<VirtualFile, Optional<GitRepository>> dirsCache = new ConcurrentHashMap<>();

  @Override
  public void disposeComponent() {
    rootsCache.clear();
    dirsCache.clear();
  }

  @Nullable
  @Override
  public GitRepository getRepoForRoot(@NotNull VirtualFile root) {
    Preconditions.checkArgument(root.isDirectory(), "%s is not a dir", root);
    return findRepoForRoot(root);
  }

  @Nullable
  private GitRepository findRepoForRoot(@NotNull VirtualFile root) {
    return rootsCache.get(root);
  }

  @Nullable
  @Override
  public GitRepository getRepoForDir(@NotNull VirtualFile dir) {
    return findRepoForDir(dir);
  }

  @Nullable
  private GitRepository findRepoForDir(@NotNull VirtualFile dir) {
    Optional<GitRepository> cachedRepo = dirsCache.get(dir);
    if (cachedRepo == null) {
      cachedRepo = calculateRepoForDir(dir);
      dirsCache.putIfAbsent(dir, cachedRepo);
      log.debug("Cached repo {} for dir {}", cachedRepo, dir);
    }
    return cachedRepo.orElse(null);
  }

  private Optional<GitRepository> calculateRepoForDir(@NotNull VirtualFile dir) {
    GitRepository foundRepo = null;
    for (VirtualFile currentDir = dir; currentDir != null; currentDir = currentDir.getParent()) {
      //TODO: could try to reuse existing dirCache data to avoid going through whole FS hierarchy
      foundRepo = rootsCache.get(currentDir);
      if (foundRepo != null) {
        break;
      }
    }
    return Optional.ofNullable(foundRepo);
  }

  @Override
  public void updatedRepoList(ImmutableList<GitRepository> repositories) {
    RepoListUpdate update = buildUpdate(repositories);
    rebuildRootsCache(update);
    purgeDirsCache(update);
  }

  private RepoListUpdate buildUpdate(ImmutableList<GitRepository> repositories) {
    Map<VirtualFile, GitRepository> mappedRepositories = repositories.stream()
        .collect(Collectors.toMap(GitRepository::getRoot, identity()));
    Set<VirtualFile> removed = new HashSet<>(rootsCache.keySet());
    removed.removeAll(mappedRepositories.keySet());
    return new RepoListUpdate(ImmutableMap.copyOf(mappedRepositories), ImmutableSet.copyOf(removed));
  }

  private void rebuildRootsCache(RepoListUpdate update) {
    Map<VirtualFile, GitRepository> newRepositories = new HashMap<>(update.repositories);
    rootsCache.keySet().forEach(newRepositories::remove);
    update.removedRoots.forEach(rootsCache::remove);
    rootsCache.putAll(newRepositories);
  }

  private void purgeDirsCache(RepoListUpdate update) {
    Set<VirtualFile> dirsToPurge = new HashSet<>(dirsCache.keySet());
    dirsToPurge.removeIf(update::isAncestorNotRemoved);
    dirsToPurge.forEach(dirsCache::remove);
  }

  private static final class RepoListUpdate {
    private final ImmutableMap<VirtualFile, GitRepository> repositories;
    private final ImmutableSet<VirtualFile> removedRoots;

    private RepoListUpdate(ImmutableMap<VirtualFile, GitRepository> repositories,
                           ImmutableSet<VirtualFile> removedRoots) {
      this.repositories = repositories;
      this.removedRoots = removedRoots;
    }

    private boolean isAncestorNotRemoved(VirtualFile file) {
      return removedRoots.stream().noneMatch(removed -> VfsUtilCore.isAncestor(removed, file, true));
    }
  }
}
