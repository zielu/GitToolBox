package zielu.gittoolbox.blame;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.metrics.GuavaCacheMetrics;
import zielu.gittoolbox.metrics.ProjectMetrics;

class BlameRevisionCacheImpl implements BlameRevisionCache, Disposable {
  private final Logger log = Logger.getInstance(getClass());
  private final Cache<VcsRevisionNumber, Blame> blames = CacheBuilder.newBuilder()
      .maximumSize(200)
      .expireAfterAccess(Duration.ofMinutes(10))
      .recordStats()
      .build();
  private final BlameCacheGateway gateway;

  BlameRevisionCacheImpl(@NotNull BlameCacheGateway gateway, @NotNull ProjectMetrics metrics) {
    metrics.addAll(new GuavaCacheMetrics(blames, "blame-revision-cache"));
    this.gateway = gateway;
    gateway.disposeWithProject(this);
  }

  @Override
  public void dispose() {
    blames.invalidateAll();
  }

  @NotNull
  @Override
  public Blame getForLine(@NotNull FileAnnotation annotation, int lineNumber) {
    VcsRevisionNumber lineRevision = annotation.getLineRevisionNumber(lineNumber);
    if (lineRevision != null) {
      try {
        return blames.get(lineRevision, () -> gateway.blameFactory().forLine(annotation, lineRevision));
      } catch (ExecutionException e) {
        log.warn("Failed to load blame for " + lineRevision + ", line " + lineNumber);
        return Blame.EMPTY;
      }
    } else {
      return Blame.EMPTY;
    }
  }

  @Override
  public Blame getForFile(@NotNull VirtualFile file, @NotNull VcsFileRevision revision) {
    if (revision != VcsFileRevision.NULL) {
      try {
        return blames.get(revision.getRevisionNumber(), () -> gateway.blameFactory().forFile(file, revision));
      } catch (ExecutionException e) {
        log.warn("Failed to load blame for " + file + ", revision " + revision);
        return Blame.EMPTY;
      }
    } else {
      return Blame.EMPTY;
    }
  }
}
