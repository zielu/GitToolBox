package zielu.gittoolbox.metrics;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.util.AppUtil;

public interface ProjectMetrics extends Metrics {

  static Metrics getInstance(@NotNull Project project) {
    return AppUtil.getServiceInstance(project, ProjectMetrics.class);
  }
}
