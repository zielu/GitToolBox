package zielu.gittoolbox.blame;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Timer;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.localVcs.UpToDateLineNumberProvider;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vfs.VirtualFile;
import java.util.concurrent.ExecutionException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.metrics.ProjectMetrics;
import zielu.gittoolbox.ui.blame.BlameUi;

class BlameServiceImpl implements BlameService, Disposable {
  private final Logger log = Logger.getInstance(getClass());
  private final BlameServiceGateway gateway;
  private final BlameCache blameCache;
  private final Cache<VirtualFile, BlameAnnotation> annotationCache = CacheBuilder.newBuilder()
      .build();
  private final Cache<Document, CachedLineProvider> lineNumberProviderCache = CacheBuilder.newBuilder()
      .weakKeys()
      .build();
  private final Timer fileBlameTimer;
  private final Timer currentLineBlameTimer;
  private final Timer documentLineBlameTimer;
  private final Counter invalidatedCounter;

  BlameServiceImpl(@NotNull BlameServiceGateway gateway, @NotNull BlameCache blameCache,
                   @NotNull ProjectMetrics metrics) {
    this.gateway = gateway;
    this.blameCache = blameCache;
    fileBlameTimer = metrics.timer("blame-file");
    currentLineBlameTimer = metrics.timer("blame-current-line");
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

  @Nullable
  @Override
  public Blame getFileBlame(@NotNull VirtualFile file) {
    return fileBlameTimer.timeSupplier(() -> getFileBlameInternal(file));
  }

  @Nullable
  private Blame getFileBlameInternal(@NotNull VirtualFile file) {
    Blame blame = null;
    try {
      VcsFileRevision revision = gateway.getLastRevision(file);
      blame = blameForRevision(revision);
    } catch (VcsException e) {
      log.warn("Failed to blame " + file, e);
    }
    return blame;
  }

  @Nullable
  private Blame blameForRevision(@Nullable VcsFileRevision revision) {
    if (revision != null && revision != VcsFileRevision.NULL) {
      return FileBlame.create(revision);
    }
    return null;
  }

  @Override
  public Blame getDocumentLineBlame(@NotNull Document document, @NotNull VirtualFile file, int editorLineNumber) {
    return documentLineBlameTimer.timeSupplier(() -> getLineBlameInternal(document, file, editorLineNumber));
  }

  @Nullable
  @Override
  public Blame getCurrentLineBlame(@NotNull Editor editor, @NotNull VirtualFile file) {
    return currentLineBlameTimer.timeSupplier(() -> getCurrentLineBlameInternal(editor, file));
  }

  @Nullable
  private Blame getCurrentLineBlameInternal(@NotNull Editor editor, @NotNull VirtualFile file) {
    Document document = editor.getDocument();
    if (invalidateOnBulkUpdate(document)) {
      return null;
    }
    int currentLine = BlameUi.getCurrentLineNumber(editor);
    if (currentLine == Integer.MIN_VALUE) {
      return null;
    }
    return getLineBlameInternal(document, file, currentLine);
  }

  private @Nullable Blame getLineBlameInternal(@NotNull Document document, @NotNull VirtualFile file,
                                               int editorLineNumber) {
    CachedLineProvider lineNumberProvider = getLineNumberProvider(document);
    if (lineNumberProvider != null) {
      if (!lineNumberProvider.isLineChanged(editorLineNumber)) {
        int correctedLine = lineNumberProvider.getLineNumber(editorLineNumber);
        return getLineBlameInternal(file, correctedLine);
      }
    }
    return null;
  }

  @Nullable
  private Blame getLineBlameInternal(@NotNull VirtualFile file, int currentLine) {
    try {
      BlameAnnotation blameAnnotation = annotationCache.get(file, () -> blameCache.getAnnotation(file));
      return blameAnnotation.getBlame(currentLine);
    } catch (ExecutionException e) {
      log.warn("Failed to blame " + file + ": " + currentLine);
      return null;
    }
  }

  private boolean invalidateOnBulkUpdate(Document document) {
    if (BlameUi.isDocumentInBulkUpdate(document)) {
      annotationCache.invalidate(document);
      return true;
    }
    return false;
  }

  @Nullable
  private CachedLineProvider getLineNumberProvider(@NotNull Document document) {
    try {
      return lineNumberProviderCache.get(document,
          () -> new CachedLineProvider(gateway.createUpToDateLineProvider(document)));
    } catch (ExecutionException e) {
      log.warn("Failed to get line number provider for " + document, e);
      return null;
    }
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

    private boolean isLineChanged(int currentNumber) {
      return lineProvider.isLineChanged(currentNumber);
    }

    private int getLineNumber(int currentNumber) {
      return lineProvider.getLineNumber(currentNumber);
    }
  }
}
