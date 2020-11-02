package zielu.gittoolbox.util;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public abstract class GatewayBase {
  protected final Project project;

  protected GatewayBase(@NotNull Project project) {
    this.project = project;
  }
}
