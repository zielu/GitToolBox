package zielu.gittoolbox.blame;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import java.util.concurrent.ExecutionException;
import org.jetbrains.annotations.NotNull;

class BlameRevisionCacheImpl implements BlameRevisionCache, Disposable {
  private final Logger log = Logger.getInstance(getClass());
  private final Cache<VcsRevisionNumber, Blame> blames = CacheBuilder.newBuilder()
      .maximumSize(100)
      .build();

  BlameRevisionCacheImpl(@NotNull BlameCacheGateway gateway) {
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
        return blames.get(lineRevision, () -> LineBlame.create(annotation, lineRevision, lineNumber));
      } catch (ExecutionException e) {
        log.warn("Failed to load blame for " + lineRevision + ", line " + lineNumber);
        return Blame.EMPTY;
      }
    } else {
      return Blame.EMPTY;
    }
  }
}
