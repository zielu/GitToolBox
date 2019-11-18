package zielu.gittoolbox.cache;

import static java.util.function.Function.identity;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.repo.GitRepository;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class VirtualFileRepoCacheImpl implements VirtualFileRepoCache, Disposable {
  private final Logger log = Logger.getInstance(getClass());
  private final ConcurrentMap<VirtualFile, GitRepository> rootsCache = new ConcurrentHashMap<>();
  private final ConcurrentMap<VirtualFile, CacheEntry> dirsCache = new ConcurrentHashMap<>();
  private final VirtualFileRepoCacheLocalGateway gateway;

  VirtualFileRepoCacheImpl(@NotNull Project project) {
    this(new VirtualFileRepoCacheLocalGatewayImpl(project));
  }

  VirtualFileRepoCacheImpl(@NotNull VirtualFileRepoCacheLocalGateway gateway) {
    this.gateway = gateway;
    gateway.rootsCacheSizeGauge(rootsCache::size);
    gateway.dirsCacheSizeGauge(dirsCache::size);
    gateway.disposeWithProject(this);
  }

  @Override
  public void dispose() {
    rootsCache.clear();
    dirsCache.clear();
  }

  @Nullable
  @Override
  public GitRepository getRepoForRoot(@NotNull VirtualFile root) {
    Preconditions.checkArgument(root.isDirectory(), "%s is not a dir", root);
    return rootsCache.get(root);
  }

  @Nullable
  @Override
  public GitRepository getRepoForDir(@NotNull VirtualFile dir) {
    Preconditions.checkArgument(dir.isDirectory(), "%s is not a dir", dir);
    return dirsCache.computeIfAbsent(dir, this::computeRepoForDir).repository;
  }

  @NotNull
  private CacheEntry computeRepoForDir(@NotNull VirtualFile dir) {
    CacheEntry entry = findRepoForDir(dir);
    log.debug("Cached repo ", entry.repository, " for dir ", dir);
    return entry;
  }

  @NotNull
  private CacheEntry findRepoForDir(@NotNull VirtualFile dir) {
    return gateway.repoForDirCacheTimer(() -> calculateRepoForDir(dir));
  }

  @NotNull
  private CacheEntry calculateRepoForDir(@NotNull VirtualFile dir) {
    GitRepository foundRepo = null;
    boolean movedUp = false;
    for (VirtualFile currentDir = dir; currentDir != null; currentDir = currentDir.getParent()) {
      if (movedUp) {
        CacheEntry existingEntry = dirsCache.get(currentDir);
        if (existingEntry != null) {
          return existingEntry;
        }
      }
      foundRepo = rootsCache.get(currentDir);
      if (foundRepo != null) {
        break;
      }
      if (!movedUp) {
        movedUp = true;
      }
    }
    return new CacheEntry(foundRepo);
  }

  @Override
  public void updatedRepoList(ImmutableList<GitRepository> repositories) {
    RepoListUpdate update = buildUpdate(repositories);
    rebuildRootsCache(update);
    purgeDirsCache(update);
    gateway.fireCacheChanged();
  }

  private RepoListUpdate buildUpdate(ImmutableList<GitRepository> repositories) {
    Map<VirtualFile, GitRepository> mappedRepositories = repositories.stream()
        .collect(Collectors.toMap(GitRepository::getRoot, identity()));
    Set<VirtualFile> removed = new HashSet<>(rootsCache.keySet());
    removed.removeAll(mappedRepositories.keySet());
    Set<VirtualFile> added = new HashSet<>(mappedRepositories.keySet());
    added.removeAll(rootsCache.keySet());
    return new RepoListUpdate(ImmutableMap.copyOf(mappedRepositories), ImmutableSet.copyOf(added),
        ImmutableSet.copyOf(removed));
  }

  private void rebuildRootsCache(RepoListUpdate update) {
    purgeRemovedRoots(update);
    addRoots(update);
  }

  private void purgeRemovedRoots(RepoListUpdate update) {
    update.removedRoots.stream()
        .peek(removed -> log.debug("Root removed: ", removed))
        .forEach(rootsCache::remove);
  }

  private void addRoots(RepoListUpdate update) {
    update.forEachAdded((root, repo) -> {
      log.debug("Root added: ", root);
      rootsCache.put(root, repo);
    });
  }

  private void purgeDirsCache(RepoListUpdate update) {
    Set<VirtualFile> dirsToPurge = new HashSet<>(dirsCache.keySet());
    dirsToPurge.removeIf(update::isAncestorKept);
    dirsToPurge.stream()
        .peek(purged -> log.debug("Purge dir: ", purged))
        .forEach(dirsCache::remove);
  }

  private static final class RepoListUpdate {
    private final ImmutableMap<VirtualFile, GitRepository> repositories;
    private final ImmutableSet<VirtualFile> addedRoots;
    private final ImmutableSet<VirtualFile> removedRoots;
    private final ImmutableSet<VirtualFile> keptRoots;

    private RepoListUpdate(ImmutableMap<VirtualFile, GitRepository> repositories,
                           ImmutableSet<VirtualFile> addedRoots,
                           ImmutableSet<VirtualFile> removedRoots) {
      this.repositories = repositories;
      this.addedRoots = addedRoots;
      this.removedRoots = removedRoots;
      Set<VirtualFile> kept = new HashSet<>(repositories.keySet());
      kept.removeAll(addedRoots);
      kept.removeAll(removedRoots);
      keptRoots = ImmutableSet.copyOf(kept);
    }

    private boolean isAncestorKept(VirtualFile file) {
      return keptRoots.stream().anyMatch(kept -> isAncestor(kept, file));
    }

    private boolean isAncestor(VirtualFile ancestor, VirtualFile file) {
      return VfsUtilCore.isAncestor(ancestor, file, false);
    }

    private void forEachAdded(BiConsumer<VirtualFile, GitRepository> consumer) {
      addedRoots.forEach(root -> {
        GitRepository repo = repositories.get(root);
        consumer.accept(root, repo);
      });
    }
  }

  private static final class CacheEntry {
    private final GitRepository repository;

    private CacheEntry(GitRepository repository) {
      this.repository = repository;
    }
  }
}
