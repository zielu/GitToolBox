package zielu.gittoolbox.metrics;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public interface ProjectMetrics extends Metrics {

  static Metrics getInstance(@NotNull Project project) {
    return project.getComponent(ProjectMetrics.class);
  }
}
