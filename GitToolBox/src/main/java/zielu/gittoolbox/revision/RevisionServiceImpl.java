package zielu.gittoolbox.revision;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class RevisionServiceImpl implements RevisionService, Disposable {
  private final Logger log = Logger.getInstance(getClass());
  private final Cache<VcsRevisionNumber, String> commitMessageCache = CacheBuilder.newBuilder()
      .maximumSize(50)
      .expireAfterAccess(Duration.ofMinutes(30))
      .recordStats()
      .build();
  private final RevisionServiceLocalGateway gateway;
  private final Project project;

  RevisionServiceImpl(@NotNull Project project) {
    this.project = project;
    gateway = new RevisionServiceLocalGateway(project);
    gateway.exposeCommitMessageCacheMetrics(commitMessageCache);
    gateway.disposeWithProject(this);
  }

  @Override
  public void dispose() {
    commitMessageCache.invalidateAll();
  }

  @NotNull
  @Override
  public RevisionInfo getForLine(@NotNull RevisionDataProvider provider, int lineNumber) {
    return RevisionInfoFactory.getInstance(project).forLine(provider, lineNumber);
  }

  @Nullable
  @Override
  public String getCommitMessage(@NotNull VirtualFile file, @NotNull RevisionInfo revisionInfo) {
    if (revisionInfo.isEmpty()) {
      return null;
    }
    VcsRevisionNumber revisionNumber = revisionInfo.getRevisionNumber();
    try {
      return commitMessageCache.get(revisionNumber, () -> gateway.loadCommitMessage(file, revisionNumber));
    } catch (ExecutionException e) {
      log.warn("Failed to load commit message " + revisionNumber, e);
      return null;
    }
  }
}
