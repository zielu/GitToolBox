package zielu.gittoolbox.revision;

import com.codahale.metrics.Timer;
import com.google.common.base.Suppliers;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.metrics.ProjectMetrics;

class RevisionServiceImpl implements RevisionService, Disposable {
  private final Logger log = Logger.getInstance(getClass());
  private final Cache<VcsRevisionNumber, Supplier<String>> commitMessageCache = CacheBuilder.newBuilder()
      .maximumSize(50)
      .expireAfterAccess(Duration.ofMinutes(30))
      .build();
  private final RevisionServiceGateway gateway;
  private final RevisionInfoFactory infoFactory;
  private final Timer loadCommitMessageTimer;

  RevisionServiceImpl(@NotNull RevisionServiceGateway gateway, @NotNull RevisionInfoFactory infoFactory,
                      @NotNull ProjectMetrics metrics) {
    this.gateway = gateway;
    this.infoFactory = infoFactory;
    metrics.gauge("commitMessageCache.size", commitMessageCache::size);
    loadCommitMessageTimer = metrics.timer("commitMessageCache.load");
    this.gateway.disposeWithProject(this);
  }

  @Override
  public void dispose() {
    commitMessageCache.invalidateAll();
  }

  @NotNull
  @Override
  public RevisionInfo getForLine(@NotNull RevisionDataProvider provider, int lineNumber) {
    return infoFactory.forLine(provider, lineNumber);
  }

  @Nullable
  @Override
  public String getCommitMessage(@NotNull VirtualFile file, @NotNull RevisionInfo revisionInfo) {
    if (revisionInfo.isEmpty()) {
      return null;
    }
    VcsRevisionNumber revisionNumber = revisionInfo.getRevisionNumber();
    try {
      return commitMessageCache.get(revisionNumber, () -> loadCommitMessage(file, revisionNumber)).get();
    } catch (ExecutionException e) {
      log.warn("Failed to load commit message " + revisionNumber, e);
      return null;
    }
  }

  private Supplier<String> loadCommitMessage(@NotNull VirtualFile file, @NotNull VcsRevisionNumber revisionNumber) {
    return loadCommitMessageTimer.timeSupplier(() -> loadCommitMessageImpl(file, revisionNumber));
  }

  private Supplier<String> loadCommitMessageImpl(@NotNull VirtualFile file, @NotNull VcsRevisionNumber revisionNumber) {
    VirtualFile root = gateway.rootForFile(file);
    if (root != null) {
      return Suppliers.ofInstance(gateway.loadCommitMessage(revisionNumber, root));
    } else {
      return Suppliers.ofInstance("");
    }
  }
}
