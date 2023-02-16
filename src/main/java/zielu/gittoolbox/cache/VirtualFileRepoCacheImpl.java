package zielu.gittoolbox.cache;

import static java.util.function.Function.identity;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.serviceContainer.NonInjectable;
import git4idea.repo.GitRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.util.GtUtil;

class VirtualFileRepoCacheImpl implements VirtualFileRepoCache, Disposable {
  private final Logger log = Logger.getInstance(getClass());
  private final ConcurrentMap<VirtualFile, GitRepository> rootsVFileCache = new ConcurrentHashMap<>();
  private final ConcurrentMap<FilePath, GitRepository> rootsFilePathCache = new ConcurrentHashMap<>();
  private final LoadingCache<VirtualFile, CacheEntry> dirsCache = CacheBuilder.newBuilder()
      .maximumSize(50)
      .weakKeys()
      .recordStats()
      .build(CacheLoader.from(this::computeRepoForDir));
  private final Cache<String, GitRepository> filePathsToRepoCache = CacheBuilder.newBuilder()
      .maximumSize(20)
      .weakKeys()
      .recordStats()
      .build();

  private final VirtualFileRepoCacheFacade facade;

  VirtualFileRepoCacheImpl(@NotNull Project project) {
    this(new VirtualFileRepoCacheFacade(project));
  }

  @NonInjectable
  VirtualFileRepoCacheImpl(@NotNull VirtualFileRepoCacheFacade facade) {
    this.facade = facade;
    facade.rootsVFileCacheSizeGauge(rootsVFileCache::size);
    facade.rootsFilePathCacheSizeGauge(rootsFilePathCache::size);
    facade.exposeDirsCacheMetrics(dirsCache);
    facade.exposeFilePathsCacheMetrics(filePathsToRepoCache);
  }

  @Override
  public void dispose() {
    rootsVFileCache.clear();
    rootsFilePathCache.clear();
    dirsCache.invalidateAll();
  }

  @Override
  public boolean hasAnyRepositories() {
    return !rootsVFileCache.isEmpty();
  }

  @Override
  public List<GitRepository> getRepositories() {
    return new ArrayList<>(rootsVFileCache.values());
  }

  @Nullable
  @Override
  public GitRepository getRepoForRoot(@NotNull VirtualFile root) {
    Preconditions.checkArgument(root.isDirectory(), "%s is not a dir", root);
    return rootsVFileCache.get(root);
  }

  @Nullable
  @Override
  public GitRepository getRepoForDir(@NotNull VirtualFile dir) {
    Preconditions.checkArgument(dir.isDirectory(), "%s is not a dir", dir);
    try {
      return dirsCache.get(dir).repository;
    } catch (ExecutionException e) {
      log.warn("Cannot compute repo for dir: " + dir, e);
      return null;
    }
  }

  @Nullable
  @Override
  public GitRepository getRepoForPath(@NotNull FilePath path) {
    String url = path.getPresentableUrl();
    GitRepository repo = filePathsToRepoCache.getIfPresent(url);
    if (repo == null) {
      repo = computeRepoForPath(path);
      if (repo != null) {
        filePathsToRepoCache.put(url, repo);
      }
    }
    return repo;
  }

  @Nullable
  private GitRepository computeRepoForPath(@NotNull FilePath path) {
    return rootsFilePathCache.keySet().stream()
               .filter(root -> path.isUnder(root, true))
               .findFirst()
               .map(rootsFilePathCache::get)
               .orElse(null);
  }

  @NotNull
  private CacheEntry computeRepoForDir(@NotNull VirtualFile dir) {
    CacheEntry entry = findRepoForDir(dir);
    log.debug("Cached repo ", entry.repository, " for dir ", dir);
    return entry;
  }

  @NotNull
  private CacheEntry findRepoForDir(@NotNull VirtualFile dir) {
    return facade.repoForDirCacheTimer(() -> calculateRepoForDir(dir));
  }

  @NotNull
  private CacheEntry calculateRepoForDir(@NotNull VirtualFile dir) {
    GitRepository foundRepo = null;
    boolean movedUp = false;
    for (VirtualFile currentDir = dir; currentDir != null; currentDir = currentDir.getParent()) {
      if (movedUp) {
        CacheEntry existingEntry = dirsCache.getIfPresent(currentDir);
        if (existingEntry != null) {
          return existingEntry;
        }
      }
      foundRepo = rootsVFileCache.get(currentDir);
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
  public void updatedRepoList(@NotNull List<GitRepository> repositories) {
    synchronized (this) {
      RepoListUpdate update = buildUpdate(repositories);
      rebuildRootsCache(update);
      purgeDirsCache(update);
      filePathsToRepoCache.invalidateAll();

      updateNotifications(update);
    }
  }

  private void updateNotifications(RepoListUpdate update) {
    if (update.hasUpdates()) {
      facade.fireCacheChanged();
    }
  }

  private RepoListUpdate buildUpdate(List<GitRepository> repositories) {
    Map<VirtualFile, GitRepository> mappedRepositories = repositories.stream()
        .collect(Collectors.toMap(GitRepository::getRoot, identity()));
    Set<VirtualFile> removed = new HashSet<>(rootsVFileCache.keySet());
    removed.removeAll(mappedRepositories.keySet());
    Set<VirtualFile> added = new HashSet<>(mappedRepositories.keySet());
    added.removeAll(rootsVFileCache.keySet());
    return new RepoListUpdate(ImmutableMap.copyOf(mappedRepositories), ImmutableSet.copyOf(added),
        ImmutableSet.copyOf(removed));
  }

  private void rebuildRootsCache(RepoListUpdate update) {
    purgeRemovedRoots(update);
    addRoots(update);
  }

  private void purgeRemovedRoots(RepoListUpdate update) {
    update.removedRoots.forEach(this::purgeRoot);
  }

  private void purgeRoot(VirtualFile root) {
    log.debug("Root removed: ", root);
    rootsVFileCache.remove(root);
    rootsFilePathCache.remove(GtUtil.localFilePath(root));
  }

  private void addRoots(RepoListUpdate update) {
    update.forEachAdded((root, repo) -> {
      log.debug("Root added: ", root);
      rootsVFileCache.put(root, repo);
      rootsFilePathCache.put(GtUtil.localFilePath(root), repo);
    });
  }

  private void purgeDirsCache(RepoListUpdate update) {
    Set<VirtualFile> dirsToPurge = new HashSet<>(dirsCache.asMap().keySet());
    dirsToPurge.removeIf(update::isAncestorKept);
    dirsToPurge.stream()
        .peek(purged -> log.debug("Purge dir: ", purged))
        .forEach(dirsCache::invalidate);
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

    private boolean hasAdditions() {
      return !addedRoots.isEmpty();
    }

    private boolean hasRemovals() {
      return !removedRoots.isEmpty();
    }

    private boolean hasUpdates() {
      return hasAdditions() || hasRemovals();
    }
  }

  private static final class CacheEntry {
    private final GitRepository repository;

    private CacheEntry(GitRepository repository) {
      this.repository = repository;
    }
  }
}
