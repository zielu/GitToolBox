package zielu.gittoolbox.metrics;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public interface ProjectMetrics extends Metrics {

  static Metrics getInstance(@NotNull Project project) {
    return ServiceManager.getService(project, ProjectMetrics.class);
  }
}
