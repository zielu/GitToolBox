package zielu.gittoolbox.revision;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Timer;
import com.google.common.base.Suppliers;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.metrics.ProjectMetrics;

class RevisionServiceImpl implements RevisionService, Disposable {
  private static final RevisionEntry EMPTY = new RevisionEntry(RevisionInfo.EMPTY, null, false);
  private final Logger log = Logger.getInstance(getClass());
  private final Cache<VcsRevisionNumber, RevisionEntry> cache = CacheBuilder.newBuilder()
      .maximumSize(500)
      .expireAfterAccess(Duration.ofMinutes(30))
      .build();
  private final RevisionServiceGateway gateway;
  private final RevisionInfoFactory infoFactory;
  private final Counter invalidatedCounter;
  private final Timer loadFileTimer;
  private final Timer loadLineTimer;

  RevisionServiceImpl(@NotNull RevisionServiceGateway gateway, @NotNull RevisionInfoFactory infoFactory,
                      @NotNull ProjectMetrics metrics) {
    this.gateway = gateway;
    this.infoFactory = infoFactory;
    metrics.gauge("revision-cache.size", cache::size);
    invalidatedCounter = metrics.counter("revision-cache.invalidated-count");
    loadFileTimer = metrics.timer("revision-cache.load-file");
    loadLineTimer = metrics.timer("revision-cache.load-line");
    this.gateway.disposeWithProject(this);
  }

  @Override
  public void dispose() {
    cache.invalidateAll();
  }

  @NotNull
  @Override
  public RevisionInfo getForLine(@NotNull RevisionDataProvider provider, int lineNumber) {
    VcsRevisionNumber lineRevision = provider.getRevisionNumber(lineNumber);
    if (lineRevision != null) {
      RevisionEntry entry = loadEntry(provider, lineRevision, lineNumber);
      if (entry.partial) {
        invalidate(lineRevision);
        entry = loadEntry(provider, lineRevision, lineNumber);
        gateway.fireRevisionUpdated(entry.info);
      }
      return entry.info;
    }
    return RevisionInfo.EMPTY;
  }

  private RevisionEntry loadEntry(@NotNull RevisionDataProvider provider, @NotNull VcsRevisionNumber lineRevision,
                                  int lineNumber) {
    try {
      return cache.get(lineRevision, () -> loadEntry(loadLineTimer,
          () -> new RevisionEntry(infoFactory.forLine(provider, lineRevision, lineNumber),
              findRoot(provider), false))
      );
    } catch (ExecutionException e) {
      log.warn("Failed to load revision " + lineRevision + " for line " + lineNumber);
      return EMPTY;
    }
  }

  @NotNull
  private RevisionEntry loadEntry(Timer timer, Supplier<RevisionEntry> loader) {
    return timer.timeSupplier(loader);
  }

  @Nullable
  private VirtualFile findRoot(@NotNull RevisionDataProvider provider) {
    VirtualFile file = provider.getFile();
    if (file != null) {
      return gateway.rootForFile(file);
    }
    return null;
  }

  private void invalidate(@NotNull VcsRevisionNumber revisionNumber) {
    cache.invalidate(revisionNumber);
    invalidatedCounter.inc();
  }

  @Override
  public RevisionInfo getForFile(@NotNull VirtualFile file, @NotNull VcsFileRevision revision) {
    if (revision != VcsFileRevision.NULL) {
      try {
        RevisionEntry entry = cache.get(revision.getRevisionNumber(), () ->
            loadEntry(loadFileTimer,
                () -> new RevisionEntry(infoFactory.forFile(file, revision),
                    gateway.rootForFile(file), true)));
        return entry.info;
      } catch (ExecutionException e) {
        log.warn("Failed to load revision " + revision + " for " + file);
        return RevisionInfo.EMPTY;
      }
    } else {
      return RevisionInfo.EMPTY;
    }
  }

  @Nullable
  @Override
  public String getDetails(@NotNull VcsRevisionNumber revisionNumber) {
    RevisionEntry entry = cache.getIfPresent(revisionNumber);
    if (entry != null) {
      Supplier<String> detailsSupplier = entry.details;
      if (detailsSupplier == null) {
        entry.details = loadDetails(revisionNumber, entry.root);
      }
      return entry.details.get();
    }
    return null;
  }

  private Supplier<String> loadDetails(@NotNull VcsRevisionNumber revisionNumber, @Nullable VirtualFile root) {
    if (root == null) {
      return Suppliers.ofInstance(null);
    } else {
      return Suppliers.ofInstance(gateway.loadDetails(revisionNumber, root));
    }
  }

  private static class RevisionEntry {
    private final RevisionInfo info;
    private final VirtualFile root;
    private final boolean partial;
    private Supplier<String> details;

    private RevisionEntry(RevisionInfo info, VirtualFile root, boolean partial) {
      this.info = info;
      this.root = root;
      this.partial = partial;
    }
  }
}
