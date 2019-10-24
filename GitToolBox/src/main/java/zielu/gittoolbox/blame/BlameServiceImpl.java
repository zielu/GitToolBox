package zielu.gittoolbox.blame;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.localVcs.UpToDateLineNumberProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import java.util.concurrent.ExecutionException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.revision.RevisionInfo;

class BlameServiceImpl implements BlameService, Disposable {
  private final Logger log = Logger.getInstance(getClass());
  private final BlameServiceLocalGateway gateway;
  private final Cache<Document, CachedLineProvider> lineNumberProviderCache = CacheBuilder.newBuilder()
      .weakKeys()
      .build();

  BlameServiceImpl(@NotNull Project project) {
    gateway = new BlameServiceLocalGateway(project);
    gateway.disposeWithProject(this);
  }

  @Override
  public void dispose() {
    lineNumberProviderCache.invalidateAll();
  }

  @NotNull
  @Override
  public RevisionInfo getDocumentLineIndexBlame(@NotNull Document document, @NotNull VirtualFile file,
                                                int lineIndex) {
    return gateway.getLineBlameTimer().timeSupplier(() -> getLineBlameInternal(document, file, lineIndex));
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
    try {
      return lineNumberProviderCache.get(document, () -> loadLineProvider(document));
    } catch (ExecutionException e) {
      log.warn("Failed to get line number provider for " + document, e);
      return null;
    }
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
    gateway.fireBlameInvalidated(file);
  }

  @Override
  public void blameUpdated(@NotNull VirtualFile file, @NotNull BlameAnnotation annotation) {
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
