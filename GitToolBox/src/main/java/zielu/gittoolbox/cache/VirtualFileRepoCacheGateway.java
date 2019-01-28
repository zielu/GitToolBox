package zielu.gittoolbox.cache;

import static zielu.gittoolbox.cache.VirtualFileRepoCache.CACHE_CHANGE;

import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.metrics.Metrics;
import zielu.gittoolbox.metrics.ProjectMetrics;

class VirtualFileRepoCacheGateway {
  private final Metrics metrics;
  private final MessageBus messageBus;

  VirtualFileRepoCacheGateway(@NotNull Project project) {
    metrics = ProjectMetrics.getInstance(project);
    messageBus = project.getMessageBus();
  }

  Metrics getMetrics() {
    return metrics;
  }

  void fireCacheChanged() {
    VirtualFileCacheListener publisher = messageBus.syncPublisher(CACHE_CHANGE);
    publisher.updated();
  }
}
