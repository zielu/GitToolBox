package zielu.gittoolbox.cache;

import static java.util.function.Function.identity;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBus;
import git4idea.repo.GitRepository;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.metrics.Metrics;
import zielu.gittoolbox.metrics.MetricsHost;

class VirtualFileRepoCacheImpl implements VirtualFileRepoCache, ProjectComponent {
  private final Logger log = Logger.getInstance(getClass());
  private final ConcurrentMap<VirtualFile, GitRepository> rootsCache = new ConcurrentHashMap<>();
  private final ConcurrentMap<VirtualFile, Optional<GitRepository>> dirsCache = new ConcurrentHashMap<>();
  private final Project project;
  private MessageBus messageBus;

  VirtualFileRepoCacheImpl(Project project) {
    this.project = project;
    Metrics metrics = MetricsHost.app();
    metrics.gauge("vfile-repo-roots-cache-size", rootsCache::size);
    metrics.gauge("vfile-repo-dirs-cache-size", dirsCache::size);
  }

  @Override
  public void initComponent() {
    messageBus = project.getMessageBus();
  }

  @Override
  public void disposeComponent() {
    messageBus = null;
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
    Optional<GitRepository> cachedRepo = dirsCache.get(dir);
    if (cachedRepo == null) {
      cachedRepo = findRepoForDir(dir);
      dirsCache.putIfAbsent(dir, cachedRepo);
      log.debug("Cached repo ", cachedRepo, " for dir ", dir);
    }
    return cachedRepo.orElse(null);
  }

  @NotNull
  private Optional<GitRepository> findRepoForDir(@NotNull VirtualFile dir) {
    return MetricsHost.app().timer("repo-for-dir-cache")
        .timeSupplier(() -> calculateRepoForDir(dir));
  }

  @NotNull
  private Optional<GitRepository> calculateRepoForDir(@NotNull VirtualFile dir) {
    GitRepository foundRepo = null;
    boolean movedUp = false;
    for (VirtualFile currentDir = dir; currentDir != null; currentDir = currentDir.getParent()) {
      if (movedUp) {
        Optional<GitRepository> existingRepo = dirsCache.get(currentDir);
        if (existingRepo != null) {
          foundRepo = existingRepo.orElse(null);
          break;
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
    return Optional.ofNullable(foundRepo);
  }

  @Override
  public void updatedRepoList(ImmutableList<GitRepository> repositories) {
    RepoListUpdate update = buildUpdate(repositories);
    rebuildRootsCache(update);
    purgeDirsCache(update);
    VirtualFileCacheListener publisher = messageBus.syncPublisher(CACHE_CHANGE);
    publisher.updated();
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
}
