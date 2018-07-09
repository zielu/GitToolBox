package zielu.gittoolbox.metrics;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class MetricsHost {
  private MetricsHost() {
    throw new IllegalStateException();
  }

  @NotNull
  public static Metrics app() {
    return GitToolBoxAppMetrics.getInstance();
  }

  @NotNull
  public static Metrics project(@NotNull Project project) {
    return GitToolBoxProjectMetrics.getInstance(project);
  }
}
