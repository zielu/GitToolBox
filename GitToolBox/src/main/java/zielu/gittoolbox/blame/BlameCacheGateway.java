package zielu.gittoolbox.blame;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBus;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.util.DisposeSafeRunnable;
import zielu.gittoolbox.util.GatewayBase;

class BlameCacheGateway extends GatewayBase {
  private final MessageBus messageBus;

  BlameCacheGateway(@NotNull Project project) {
    super(project);
    messageBus = project.getMessageBus();
  }

  void fireBlameUpdated(@NotNull VirtualFile file, @NotNull BlameAnnotation annotation) {
    runInBackground(() -> messageBus.syncPublisher(BlameCache.CACHE_UPDATES).cacheUpdated(file, annotation));
  }

  private void runInBackground(Runnable task) {
    ApplicationManager.getApplication().executeOnPooledThread(new DisposeSafeRunnable(project, task));
  }

  void fireBlameInvalidated(@NotNull VirtualFile file) {
    runInBackground(() -> messageBus.syncPublisher(BlameCache.CACHE_UPDATES).invalidated(file));
  }
}
