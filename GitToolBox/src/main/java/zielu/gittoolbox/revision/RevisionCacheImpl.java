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
  private static final RevisionEntry EMPTY = new RevisionEntry(RevisionInfo.EMPTY, false);
  private final Logger log = Logger.getInstance(getClass());
  private final Cache<VcsRevisionNumber, RevisionEntry> cache = CacheBuilder.newBuilder()
      .maximumSize(500)
      .expireAfterAccess(Duration.ofMinutes(30))
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
      RevisionEntry entry = loadEntry(annotation, lineRevision, lineNumber);
      if (entry.partial) {
        cache.invalidate(lineRevision);
        entry = loadEntry(annotation, lineRevision, lineNumber);
      }
      return entry.info;
    }
    return RevisionInfo.EMPTY;
  }

  private RevisionEntry loadEntry(@NotNull FileAnnotation annotation, @NotNull VcsRevisionNumber lineRevision,
                                  int lineNumber) {
    try {
      return cache.get(lineRevision, () -> new RevisionEntry(
          infoFactory.forLine(annotation, lineRevision, lineNumber), false));
    } catch (ExecutionException e) {
      log.warn("Failed to load revision " + lineRevision + " for line " + lineNumber);
      return EMPTY;
    }
  }

  @Override
  public RevisionInfo getForFile(@NotNull VirtualFile file, @NotNull VcsFileRevision revision) {
    if (revision != VcsFileRevision.NULL) {
      try {
        RevisionEntry entry = cache.get(revision.getRevisionNumber(),
            () -> new RevisionEntry(infoFactory.forFile(file, revision), true));
        return entry.info;
      } catch (ExecutionException e) {
        log.warn("Failed to load revision " + revision +  " for " + file);
        return RevisionInfo.EMPTY;
      }
    } else {
      return RevisionInfo.EMPTY;
    }
  }

  private static class RevisionEntry {
    private final RevisionInfo info;
    private final boolean partial;

    private RevisionEntry(RevisionInfo info, boolean partial) {
      this.info = info;
      this.partial = partial;
    }
  }
}
