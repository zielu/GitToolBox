package zielu.gittoolbox.blame;

import com.codahale.metrics.Timer;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ex.DocumentEx;
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
import zielu.gittoolbox.metrics.Metrics;
import zielu.gittoolbox.metrics.MetricsHost;
import zielu.gittoolbox.util.GtUtil;

class BlameServiceImpl implements BlameService {
  private final Logger log = Logger.getInstance(getClass());
  private final Project project;
  private final BlameCache blameCache;
  private final Cache<Document, BlameAnnotation> annotationCache = CacheBuilder.newBuilder()
      .weakKeys()
      .removalListener(this::onAnnotationCacheRemoval)
      .build();
  private final Cache<Document, CachedLineProvider> lineNumberProviderCache = CacheBuilder.newBuilder()
      .weakKeys()
      .build();
  private final Timer fileBlameTimer;
  private final Timer lineBlameTimer;
  private final MessageBusConnection connection;

  BlameServiceImpl(@NotNull Project project, @NotNull BlameCache blameCache) {
    this.project = project;
    this.blameCache = blameCache;
    Metrics metrics = MetricsHost.project(project);
    fileBlameTimer = metrics.timer("blame-file-blame");
    lineBlameTimer = metrics.timer("blame-line-blame");
    metrics.gauge("blame-annotation-cache-size", annotationCache::size);
    connection = project.getMessageBus().connect(project);
    connection.subscribe(BlameCache.TOPIC, new BlameCacheListener() {
      @Override
      public void cacheUpdated(@NotNull VirtualFile file, @NotNull BlameAnnotation annotation) {
        project.getMessageBus().syncPublisher(BLAME_UPDATE).blameUpdated(file);
      }
    });
  }

  private void onAnnotationCacheRemoval(RemovalNotification<Document, BlameAnnotation> entry) {
    VirtualFile file = entry.getValue().getVirtualFile();
    if (file != null) {
      blameCache.invalidate(file);
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

  @Nullable
  @Override
  public Blame getCurrentLineBlame(@NotNull Editor editor, @NotNull VirtualFile file) {
    return lineBlameTimer.timeSupplier(() -> getCurrentLineBlameInternal(editor, file));
  }

  private int getCurrentLineNumber(Editor editor) {
    CaretModel caretModel = editor.getCaretModel();
    if (!caretModel.isUpToDate()) {
      return Integer.MIN_VALUE;
    }
    LogicalPosition position = caretModel.getLogicalPosition();
    return position.line;
  }

  private boolean isDocumentInBulkUpdate(Document document) {
    if (document instanceof DocumentEx) {
      DocumentEx docEx = (DocumentEx) document;
      return docEx.isInBulkUpdate();
    }
    return false;
  }

  @Nullable
  private Blame getCurrentLineBlameInternal(@NotNull Editor editor, @NotNull VirtualFile file) {
    Document document = editor.getDocument();
    if (invalidateOnBulkUpdate(document)) {
      return null;
    }
    int currentLine = getCurrentLineNumber(editor);
    if (currentLine == Integer.MIN_VALUE) {
      return null;
    }
    CachedLineProvider lineNumberProvider = getLineNumberProvider(document);
    if (lineNumberProvider != null) {
      if (!lineNumberProvider.isLineChanged(currentLine)) {
        int correctedLine = lineNumberProvider.getLineNumber(currentLine);
        return getCurrentLineBlameInternal(document, file, correctedLine);
      }
    }
    return null;
  }

  @Nullable
  private Blame getCurrentLineBlameInternal(@NotNull Document document, @NotNull VirtualFile file,
                                            int currentLine) {
    try {
      BlameAnnotation blameAnnotation = annotationCache.get(document, () -> blameCache.getAnnotation(file));
      return blameAnnotation.getBlame(currentLine);
    } catch (ExecutionException e) {
      log.warn("Failed to blame " + file + ": " + currentLine);
      return null;
    }
  }

  private boolean invalidateOnBulkUpdate(Document document) {
    if (isDocumentInBulkUpdate(document)) {
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
