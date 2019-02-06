package zielu.gittoolbox.blame;

import com.codahale.metrics.Timer;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.localVcs.UpToDateLineNumberProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.impl.UpToDateLineNumberProviderImpl;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBusConnection;
import git4idea.GitVcs;
import java.util.concurrent.ExecutionException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.metrics.ProjectMetrics;
import zielu.gittoolbox.ui.blame.BlameUi;
import zielu.gittoolbox.util.GtUtil;

class BlameServiceImpl implements BlameService, Disposable {
  private final Logger log = Logger.getInstance(getClass());
  private final Project project;
  private final BlameCache blameCache;
  private final Cache<VirtualFile, BlameAnnotation> annotationCache = CacheBuilder.newBuilder()
      .build();
  private final Cache<Document, CachedLineProvider> lineNumberProviderCache = CacheBuilder.newBuilder()
      .weakKeys()
      .build();
  private final Timer fileBlameTimer;
  private final Timer currentLineBlameTimer;
  private final Timer documentLineBlameTimer;
  private MessageBusConnection connection;

  BlameServiceImpl(@NotNull Project project, @NotNull BlameCache blameCache, @NotNull ProjectMetrics metrics) {
    this.project = project;
    this.blameCache = blameCache;
    fileBlameTimer = metrics.timer("blame-file");
    currentLineBlameTimer = metrics.timer("blame-current-line");
    documentLineBlameTimer = metrics.timer("blame-document-line");
    metrics.gauge("blame-annotation-cache-size", annotationCache::size);
    connection = project.getMessageBus().connect();
    connection.subscribe(BlameCache.CACHE_UPDATES, new BlameCacheListener() {
      @Override
      public void cacheUpdated(@NotNull VirtualFile file, @NotNull BlameAnnotation annotation) {
        annotationCache.put(file, annotation);
        project.getMessageBus().syncPublisher(BLAME_UPDATE).blameUpdated(file);
      }

      @Override
      public void invalidated(@NotNull VirtualFile file) {
        annotationCache.invalidate(file);
        project.getMessageBus().syncPublisher(BLAME_UPDATE).blameInvalidated(file);
      }
    });
  }

  @Override
  public void dispose() {
    if (connection != null) {
      connection.disconnect();
      connection = null;
    }
  }

  @Nullable
  @Override
  public Blame getFileBlame(@NotNull VirtualFile file) {
    return fileBlameTimer.timeSupplier(() -> getFileBlameInternal(file));
  }

  @Nullable
  private Blame getFileBlameInternal(@NotNull VirtualFile file) {
    GitVcs git = getGit();
    Blame blame = null;
    try {
      VcsFileRevision revision = git.getVcsHistoryProvider().getLastRevision(GtUtil.localFilePath(file));
      blame = blameForRevision(revision);
    } catch (VcsException e) {
      log.warn("Failed to blame " + file, e);
    }
    return blame;
  }

  private GitVcs getGit() {
    return GitVcs.getInstance(project);
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
          () -> new CachedLineProvider(new UpToDateLineNumberProviderImpl(document, project)));
    } catch (ExecutionException e) {
      log.warn("Failed to get line number provider for " + document, e);
      return null;
    }
  }

  @Override
  public void fileClosed(@NotNull VirtualFile file) {
    blameCache.invalidate(file);
  }

  private static final class CachedLineProvider {
    private final UpToDateLineNumberProvider lineProvider;

    private CachedLineProvider(UpToDateLineNumberProvider lineProvider) {
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
