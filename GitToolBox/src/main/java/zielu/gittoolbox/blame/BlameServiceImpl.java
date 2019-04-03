package zielu.gittoolbox.blame;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Timer;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.localVcs.UpToDateLineNumberProvider;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vfs.VirtualFile;
import java.util.concurrent.ExecutionException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.metrics.ProjectMetrics;
import zielu.gittoolbox.revision.RevisionInfo;
import zielu.gittoolbox.revision.RevisionService;
import zielu.gittoolbox.ui.blame.BlameUi;

class BlameServiceImpl implements BlameService, Disposable {
  private final Logger log = Logger.getInstance(getClass());
  private final BlameServiceGateway gateway;
  private final BlameCache blameCache;
  private final RevisionService revisionService;
  private final Cache<VirtualFile, BlameAnnotation> annotationCache = CacheBuilder.newBuilder()
      .build();
  private final Cache<Document, CachedLineProvider> lineNumberProviderCache = CacheBuilder.newBuilder()
      .weakKeys()
      .build();
  private final Timer fileBlameTimer;
  private final Timer documentLineBlameTimer;
  private final Counter invalidatedCounter;

  BlameServiceImpl(@NotNull BlameServiceGateway gateway, @NotNull BlameCache blameCache,
                   @NotNull RevisionService revisionService, @NotNull ProjectMetrics metrics) {
    this.gateway = gateway;
    this.blameCache = blameCache;
    this.revisionService = revisionService;
    fileBlameTimer = metrics.timer("blame-file");
    documentLineBlameTimer = metrics.timer("blame-document-line");
    invalidatedCounter = metrics.counter("blame-annotation-invalidated-count");
    metrics.gauge("blame-annotation-cache-size", annotationCache::size);
    this.gateway.disposeWithProject(this);
  }

  @Override
  public void dispose() {
    annotationCache.invalidateAll();
    lineNumberProviderCache.invalidateAll();
  }

  @NotNull
  @Override
  public RevisionInfo getFileBlame(@NotNull VirtualFile file) {
    return fileBlameTimer.timeSupplier(() -> getFileBlameInternal(file));
  }

  @NotNull
  private RevisionInfo getFileBlameInternal(@NotNull VirtualFile file) {
    RevisionInfo revisionInfo = RevisionInfo.EMPTY;
    try {
      VcsFileRevision revision = gateway.getLastRevision(file);
      revisionInfo = blameForRevision(file, revision);
    } catch (VcsException e) {
      log.warn("Failed to revisionInfo " + file, e);
    }
    return revisionInfo;
  }

  @NotNull
  private RevisionInfo blameForRevision(@NotNull VirtualFile file, @Nullable VcsFileRevision revision) {
    if (revision != null) {
      return revisionService.getForFile(file, revision);
    }
    return RevisionInfo.EMPTY;
  }

  @NotNull
  @Override
  public RevisionInfo getDocumentLineIndexBlame(@NotNull Document document, @NotNull VirtualFile file,
                                                int lineIndex) {
    return documentLineBlameTimer.timeSupplier(() -> getLineBlameInternal(document, file, lineIndex));
  }

  @NotNull
  private RevisionInfo getLineBlameInternal(@NotNull Document document, @NotNull VirtualFile file,
                                            int lineIndex) {
    if (invalidateOnBulkUpdate(document, file)) {
      return RevisionInfo.EMPTY;
    }
    CachedLineProvider lineProvider = getLineProvider(document);
    if (lineProvider != null) {
      if (!lineProvider.isLineChanged(lineIndex)) {
        int correctedLineIndex = lineProvider.getLineIndex(lineIndex);
        return getLineBlameInternal(file, correctedLineIndex);
      }
    }
    return RevisionInfo.EMPTY;
  }

  @NotNull
  private RevisionInfo getLineBlameInternal(@NotNull VirtualFile file, int lineIndex) {
    try {
      BlameAnnotation blameAnnotation = annotationCache.get(file, () -> blameCache.getAnnotation(file));
      return blameAnnotation.getBlame(lineIndex);
    } catch (ExecutionException e) {
      log.warn("Failed to blame " + file + ": " + (lineIndex + 1));
      return RevisionInfo.EMPTY;
    }
  }

  private boolean invalidateOnBulkUpdate(@NotNull Document document, @NotNull VirtualFile file) {
    if (BlameUi.isDocumentInBulkUpdate(document)) {
      annotationCache.invalidate(file);
      return true;
    }
    return false;
  }

  @Nullable
  private CachedLineProvider getLineProvider(@NotNull Document document) {
    try {
      return lineNumberProviderCache.get(document, () -> loadLineProvider(document));
    } catch (ExecutionException e) {
      log.warn("Failed to get line number provider for " + document, e);
      return null;
    }
  }

  private CachedLineProvider loadLineProvider(@NotNull Document document) {
    return new CachedLineProvider(gateway.createUpToDateLineProvider(document));
  }

  @Override
  public void fileClosed(@NotNull VirtualFile file) {
    blameCache.invalidate(file);
  }

  @Override
  public void invalidate(@NotNull VirtualFile file) {
    annotationCache.invalidate(file);
    invalidatedCounter.inc();
    gateway.fireBlameInvalidated(file);
  }

  @Override
  public void blameUpdated(@NotNull VirtualFile file, @NotNull BlameAnnotation annotation) {
    annotationCache.put(file, annotation);
    gateway.fireBlameUpdated(file);
  }

  private static final class CachedLineProvider {
    private final UpToDateLineNumberProvider lineProvider;

    private CachedLineProvider(@NotNull UpToDateLineNumberProvider lineProvider) {
      this.lineProvider = lineProvider;
    }

    private boolean isLineChanged(int currentIndex) {
      return lineProvider.isLineChanged(currentIndex);
    }

    private int getLineIndex(int currentNumber) {
      return lineProvider.getLineNumber(currentNumber);
    }
  }
}
