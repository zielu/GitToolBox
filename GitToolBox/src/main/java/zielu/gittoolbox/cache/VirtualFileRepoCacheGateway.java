package zielu.gittoolbox.cache;

import static zielu.gittoolbox.cache.VirtualFileRepoCache.CACHE_CHANGE;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.metrics.Metrics;
import zielu.gittoolbox.metrics.ProjectMetrics;

class VirtualFileRepoCacheGateway implements ProjectComponent {
  private final Project project;
  private MessageBus messageBus;

  VirtualFileRepoCacheGateway(@NotNull Project project) {
    this.project = project;
  }

  @Override
  public void initComponent() {
    messageBus = project.getMessageBus();
  }

  @Override
  public void disposeComponent() {
    messageBus = null;
  }

  Metrics getMetrics() {
    return ProjectMetrics.getInstance(project);
  }

  void fireCacheChanged() {
    VirtualFileCacheListener publisher = messageBus.syncPublisher(CACHE_CHANGE);
    publisher.updated();
  }
}
