package zielu.gittoolbox.blame;

import com.codahale.metrics.Timer;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.DocumentEx;
import com.intellij.openapi.localVcs.UpToDateLineNumberProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vcs.impl.UpToDateLineNumberProviderImpl;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.GitVcs;
import git4idea.repo.GitRepository;
import gnu.trove.TIntObjectHashMap;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.cache.VirtualFileRepoCache;
import zielu.gittoolbox.metrics.Metrics;
import zielu.gittoolbox.metrics.MetricsHost;
import zielu.gittoolbox.util.GtUtil;

class BlameServiceImpl implements BlameService {
  private static final UpToDateLineNumberProvider EMPTY_PROVIDER = new UpToDateLineNumberProvider() {
    @Override
    public int getLineCount() {
      return 0;
    }

    @Override
    public int getLineNumber(int currentNumber) {
      return ABSENT_LINE_NUMBER;
    }

    @Override
    public boolean isLineChanged(int currentNumber) {
      return true;
    }

    @Override
    public boolean isRangeChanged(int start, int end) {
      return true;
    }
  };
  private final Logger log = Logger.getInstance(getClass());
  private final Project project;
  private final VirtualFileRepoCache repoCache;
  private final GitVcs git;
  private final Cache<Document, CachedAnnotation> annotationCache = CacheBuilder.newBuilder()
      .weakKeys()
      .build();
  private final Cache<Document, CachedBlames> blameCache = CacheBuilder.newBuilder()
      .weakKeys()
      .build();
  private final Cache<Document, UpToDateLineNumberProvider> lineNumberProviderCache = CacheBuilder.newBuilder()
      .weakKeys()
      .build();
  private final Timer fileBlameTimer;
  private final Timer lineBlameTimer;
  private final Timer annotationTimer;
  private final Timer currentRevisionTimer;

  BlameServiceImpl(@NotNull Project project) {
    this.project = project;
    repoCache = VirtualFileRepoCache.getInstance(project);
    git = GitVcs.getInstance(project);
    Metrics metrics = MetricsHost.project(project);
    fileBlameTimer = metrics.timer("blame-file-blame");
    lineBlameTimer = metrics.timer("blame-line-blame");
    annotationTimer = metrics.timer("blame-annotation");
    currentRevisionTimer = metrics.timer("blame-current-repoRevision");
    metrics.gauge("blame-annotation-cache-size", annotationCache::size);
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
    return caretModel.getLogicalPosition().line;
  }

  private boolean isDocumentInInvalidState(Document document) {
    if (document instanceof DocumentEx) {
      DocumentEx docEx = (DocumentEx) document;
      return docEx.isInBulkUpdate();
    }
    return false;
  }

  @Nullable
  private Blame getCurrentLineBlameInternal(@NotNull Editor editor, @NotNull VirtualFile file) {
    Document document = editor.getDocument();
    int currentLine = getCurrentLineNumber(editor);
    if (currentLine == Integer.MIN_VALUE) {
      return null;
    }
    if (document.isLineModified(currentLine)) {
      //return file blame
      return null;
    }
    UpToDateLineNumberProvider lineNumberProvider = getLineNumberProvider(document);
    if (lineNumberProvider.isLineChanged(currentLine)) {
      return null;
    } else {
      int correctedLine = lineNumberProvider.getLineNumber(currentLine);
      return getCurrentLineBlameInternal(document, file, correctedLine);
    }
  }

  @Nullable
  private Blame getCurrentLineBlameInternal(@NotNull Document document, @NotNull VirtualFile file,
                                            int currentLine) {
    VcsRevisionNumber repoRevision = currentRevisionTimer.timeSupplier(() -> currentRepoRevision(file));
    Blame cachedBlame = getCachedBlame(document, repoRevision, currentLine);
    if (cachedBlame != null) {
      return cachedBlame;
    }
    return cacheBlame(document, file, repoRevision, currentLine);
  }

  @NotNull
  private UpToDateLineNumberProvider getLineNumberProvider(@NotNull Document document) {
    try {
      return lineNumberProviderCache.get(document,
          () -> new UpToDateLineNumberProviderImpl(document, project));
    } catch (ExecutionException e) {
      log.warn("Failed to get line number provider for " + document, e);
      return EMPTY_PROVIDER;
    }
  }

  @Nullable
  private Blame getCachedBlame(@NotNull Document document, @NotNull VcsRevisionNumber repoRevision, int line) {
    CachedBlames cachedBlames = blameCache.getIfPresent(document);
    if (cachedBlames != null) {
      if (cachedBlames.isRevisionChanged(repoRevision)) {
        blameCache.invalidate(document);
      } else {
        return cachedBlames.getBlame(line);
      }
    }
    return null;
  }

  @Nullable
  private Blame cacheBlame(@NotNull Document document, @NotNull VirtualFile file,
                           @NotNull VcsRevisionNumber repoRevision, int line) {
    FileAnnotation annotation = getAnnotation(document, repoRevision, file);
    if (annotation != null) {
      CachedBlames blames = cachedBlames(document, repoRevision);
      return blames.putBlame(line, LineBlame.create(annotation, line));
    }
    return null;
  }

  private CachedBlames cachedBlames(@NotNull Document document, @NotNull VcsRevisionNumber repoRevision) {
    try {
      return blameCache.get(document, () -> new CachedBlames(repoRevision));
    } catch (ExecutionException e) {
      throw new IllegalStateException("Failed to load cache blames " + document, e);
    }
  }

  @Nullable
  private FileAnnotation getAnnotation(@NotNull Document document, @NotNull VcsRevisionNumber repoRevision,
                                       @NotNull VirtualFile file) {
    CachedAnnotation cachedAnnotation = getCachedAnnotation(document, repoRevision, file);
    if (cachedAnnotation != null) {
      if (cachedAnnotation.isRevisionChanged(repoRevision)) {
        annotationCache.invalidate(document);
        cachedAnnotation = getCachedAnnotation(document, repoRevision, file);
      }
    }
    if (cachedAnnotation != null) {
      return cachedAnnotation.annotation;
    }
    return null;
  }

  private VcsRevisionNumber currentRepoRevision(@NotNull VirtualFile file) {
    GitRepository repo = repoCache.getRepoForFile(file);
    if (repo != null) {
      try {
        return git.parseRevisionNumber(repo.getCurrentRevision());
      } catch (VcsException e) {
        log.warn("Could not get current repoRevision for " + file);
      }
    }
    return VcsRevisionNumber.NULL;
  }

  @Nullable
  private CachedAnnotation getCachedAnnotation(@NotNull Document document, @NotNull VcsRevisionNumber repoRevision,
                                               @NotNull VirtualFile file) {
    try {
      return annotationCache.get(document, () -> annotationTimer.time(() -> {
        FileAnnotation annotation = loadAnnotation(file);
        return new CachedAnnotation(repoRevision, annotation);
      }));
    } catch (Exception e) {
      log.warn("Failed to get cached annotation for " + file, e);
    }
    return null;
  }

  @Nullable
  private FileAnnotation loadAnnotation(@NotNull VirtualFile file) {
    try {
      return getGit().getAnnotationProvider().annotate(file);
    } catch (VcsException e) {
      log.warn("Failed to annotate " + file, e);
    }
    return null;
  }

  @Override
  public void fileClosed(@NotNull VirtualFile file) {
  }

  private static final class CachedAnnotation {
    private VcsRevisionNumber repoRevision;
    private final FileAnnotation annotation;

    private CachedAnnotation(@NotNull VcsRevisionNumber repoRevision, FileAnnotation annotation) {
      this.repoRevision = repoRevision;
      this.annotation = annotation;
    }

    private boolean isRevisionChanged(VcsRevisionNumber repoRevision) {
      return !Objects.equals(this.repoRevision, repoRevision);
    }
  }

  private static final class CachedBlames {
    private final VcsRevisionNumber repoRevision;
    private final TIntObjectHashMap<Blame> blames = new TIntObjectHashMap<>();

    private CachedBlames(VcsRevisionNumber repoRevision) {
      this.repoRevision = repoRevision;
    }

    private boolean isRevisionChanged(VcsRevisionNumber repoRevision) {
      return !Objects.equals(this.repoRevision, repoRevision);
    }

    @Nullable
    public Blame getBlame(int lineNumber) {
      return blames.get(lineNumber);
    }

    public Blame putBlame(int lineNumber, @NotNull Blame blame) {
      return blames.put(lineNumber, blame);
    }
  }
}
