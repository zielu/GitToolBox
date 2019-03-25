package zielu.gittoolbox.revision;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.util.GatewayBase;

class RevisionServiceGateway extends GatewayBase {
  RevisionServiceGateway(@NotNull Project project) {
    super(project);
  }

  void fireRevisionUpdated(RevisionInfo revisionInfo) {
    project.getMessageBus().syncPublisher(RevisionService.UPDATES).revisionUpdated(revisionInfo);
  }
}
