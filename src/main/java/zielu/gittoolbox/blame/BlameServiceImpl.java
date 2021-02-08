package zielu.gittoolbox.blame;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.localVcs.UpToDateLineNumberProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.revision.RevisionInfo;
import zielu.intellij.util.ZDisposeGuard;

class BlameServiceImpl implements BlameService, Disposable {
  private final Logger log = Logger.getInstance(getClass());
  private final ZDisposeGuard disposeGuard = new ZDisposeGuard();
  private final BlameServiceFacade gateway;
  private final LoadingCache<Document, CachedLineProvider> lineNumberProviderCache = CacheBuilder.newBuilder()
      .weakKeys()
      .recordStats()
      .build(CacheLoader.from(this::loadLineProvider));

  BlameServiceImpl(@NotNull Project project) {
    gateway = new BlameServiceFacade(project);
    gateway.exposeCacheMetrics(lineNumberProviderCache, "blame-service-cache");
    gateway.registerDisposable(this, gateway);
    gateway.registerDisposable(this, disposeGuard);
  }

  @Override
  public void dispose() {
    lineNumberProviderCache.invalidateAll();
  }

  @NotNull
  @Override
  public RevisionInfo getDocumentLineIndexBlame(@NotNull Document document, @NotNull VirtualFile file,
                                                int lineIndex) {
    if (disposeGuard.isActive()) {
      return gateway.timeLineBlame(() -> getLineBlameInternal(document, file, lineIndex));
    }
    return RevisionInfo.NULL;
  }

  @NotNull
  private RevisionInfo getLineBlameInternal(@NotNull Document document, @NotNull VirtualFile file,
                                            int lineIndex) {
    RevisionInfo revisionInfo = RevisionInfo.NULL;
    CachedLineProvider lineProvider = getLineProvider(document);
    if (lineProvider != null && !lineProvider.isLineChanged(lineIndex)) {
      int correctedLineIndex = lineProvider.getLineIndex(lineIndex);
      revisionInfo = getLineBlameInternal(file, correctedLineIndex);
    }
    log.debug("Get line ", lineIndex, " blame for ", file, " info ", revisionInfo);
    return revisionInfo;
  }

  @NotNull
  private RevisionInfo getLineBlameInternal(@NotNull VirtualFile file, int lineIndex) {
    BlameAnnotation blameAnnotation = gateway.getAnnotation(file);
    return blameAnnotation.getBlame(lineIndex);
  }

  @Nullable
  private CachedLineProvider getLineProvider(@NotNull Document document) {
    return lineNumberProviderCache.getUnchecked(document);
  }

  private CachedLineProvider loadLineProvider(@NotNull Document document) {
    return new CachedLineProvider(gateway.lineNumberProvider(document));
  }

  @Override
  public void fileClosed(@NotNull VirtualFile file) {
    //do nothing
  }

  @Override
  public void invalidate(@NotNull VirtualFile file) {
    if (disposeGuard.isActive()) {
      gateway.fireBlameInvalidated(file);
    }
  }

  @Override
  public void blameUpdated(@NotNull VirtualFile file, @NotNull BlameAnnotation annotation) {
    if (disposeGuard.isActive()) {
      gateway.fireBlameUpdated(file);
    }
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
