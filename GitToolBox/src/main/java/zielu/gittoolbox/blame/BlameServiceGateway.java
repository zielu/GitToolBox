package zielu.gittoolbox.blame;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.localVcs.UpToDateLineNumberProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.impl.UpToDateLineNumberProviderImpl;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBus;
import git4idea.GitVcs;
import java.util.concurrent.ExecutorService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.GitToolBoxApp;
import zielu.gittoolbox.util.DisposeSafeRunnable;
import zielu.gittoolbox.util.GatewayBase;
import zielu.gittoolbox.util.GtUtil;

class BlameServiceGateway extends GatewayBase {
  private final GitVcs git;
  private final MessageBus messageBus;
  private final ExecutorService executor;

  BlameServiceGateway(@NotNull Project project, @NotNull GitToolBoxApp app) {
    super(project);
    git = GitVcs.getInstance(project);
    messageBus = project.getMessageBus();
    executor = app.tasksExecutor();
  }

  @NotNull
  UpToDateLineNumberProvider createUpToDateLineProvider(@NotNull Document document) {
    return new UpToDateLineNumberProviderImpl(document, project);
  }

  @Nullable
  VcsFileRevision getLastRevision(@NotNull VirtualFile file) throws VcsException {
    return git.getVcsHistoryProvider().getLastRevision(GtUtil.localFilePath(file));
  }

  void fireBlameUpdated(@NotNull VirtualFile file) {
    publishAsync(() -> messageBus.syncPublisher(BlameService.BLAME_UPDATE).blameUpdated(file));
  }

  private void publishAsync(Runnable task) {
    executor.submit(new DisposeSafeRunnable(project, task));
  }

  void fireBlameInvalidated(@NotNull VirtualFile file) {
    publishAsync(() -> messageBus.syncPublisher(BlameService.BLAME_UPDATE).blameInvalidated(file));
  }
}
