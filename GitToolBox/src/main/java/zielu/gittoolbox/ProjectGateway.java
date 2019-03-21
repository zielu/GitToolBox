package zielu.gittoolbox;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.util.GatewayBase;

/**
 * Generic gateway for services that don't need specialized one.
 */
public class ProjectGateway extends GatewayBase {
  ProjectGateway(@NotNull Project project) {
    super(project);
  }
}
