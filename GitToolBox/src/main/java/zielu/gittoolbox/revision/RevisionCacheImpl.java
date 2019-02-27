package zielu.gittoolbox.revision;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.metrics.GuavaCacheMetrics;
import zielu.gittoolbox.metrics.ProjectMetrics;

class RevisionCacheImpl implements RevisionCache, Disposable {
  private final Logger log = Logger.getInstance(getClass());
  private final Cache<VcsRevisionNumber, RevisionInfo> cache = CacheBuilder.newBuilder()
      .maximumSize(200)
      .expireAfterAccess(Duration.ofMinutes(10))
      .recordStats()
      .build();
  private final RevisionInfoFactory infoFactory;

  RevisionCacheImpl(@NotNull Project project, @NotNull RevisionInfoFactory infoFactory,
                    @NotNull ProjectMetrics metrics) {
    this.infoFactory = infoFactory;
    metrics.addAll(new GuavaCacheMetrics(cache, "revision-cache"));
    Disposer.register(project, this);
  }

  @Override
  public void dispose() {
    cache.invalidateAll();
  }

  @NotNull
  @Override
  public RevisionInfo getForLine(@NotNull FileAnnotation annotation, int lineNumber) {
    VcsRevisionNumber lineRevision = annotation.getLineRevisionNumber(lineNumber);
    if (lineRevision != null) {
      try {
        return cache.get(lineRevision, () -> infoFactory.forLine(annotation, lineRevision, lineNumber));
      } catch (ExecutionException e) {
        log.warn("Failed to load revision " + lineRevision + " for line " + lineNumber);
        return RevisionInfo.EMPTY;
      }
    } else {
      return RevisionInfo.EMPTY;
    }
  }

  @Override
  public RevisionInfo getForFile(@NotNull VirtualFile file, @NotNull VcsFileRevision revision) {
    if (revision != VcsFileRevision.NULL) {
      try {
        return cache.get(revision.getRevisionNumber(), () -> infoFactory.forFile(file, revision));
      } catch (ExecutionException e) {
        log.warn("Failed to load revision " + revision +  " for " + file);
        return RevisionInfo.EMPTY;
      }
    } else {
      return RevisionInfo.EMPTY;
    }
  }
}
