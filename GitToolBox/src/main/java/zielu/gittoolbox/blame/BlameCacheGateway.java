package zielu.gittoolbox.blame;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBus;
import java.util.concurrent.ExecutorService;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.GitToolBoxApp;
import zielu.gittoolbox.util.DisposeSafeRunnable;
import zielu.gittoolbox.util.GatewayBase;

class BlameCacheGateway extends GatewayBase {
  private final MessageBus messageBus;
  private final ExecutorService executor;

  BlameCacheGateway(@NotNull Project project, GitToolBoxApp app) {
    super(project);
    messageBus = project.getMessageBus();
    executor = app.tasksExecutor();
  }

  void fireBlameUpdated(@NotNull VirtualFile file, @NotNull BlameAnnotation annotation) {
    runInBackground(() -> messageBus.syncPublisher(BlameCache.CACHE_UPDATES).cacheUpdated(file, annotation));
  }

  void runInBackground(Runnable task) {
    executor.execute(new DisposeSafeRunnable(project, task));
  }

  void fireBlameInvalidated(@NotNull VirtualFile file) {
    runInBackground(() -> messageBus.syncPublisher(BlameCache.CACHE_UPDATES).invalidated(file));
  }
}
