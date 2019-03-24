package zielu.gittoolbox.fetch;

import com.intellij.openapi.project.Project;
import java.time.Clock;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.util.GatewayBase;

class AutoFetchGateway extends GatewayBase {
  private final Clock clock = Clock.systemDefaultZone();

  AutoFetchGateway(@NotNull Project project) {
    super(project);
  }

  Clock getClock() {
    return clock;
  }
}
