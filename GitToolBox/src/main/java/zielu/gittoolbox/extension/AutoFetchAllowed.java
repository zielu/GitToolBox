package zielu.gittoolbox.extension;

import com.intellij.openapi.project.Project;
import com.intellij.util.messages.Topic;

public interface AutoFetchAllowed {
  Topic<Notifier> TOPIC = Topic.create("Git ToolBox Auto Fetch Allowed", Notifier.class);

  boolean isAllowed();

  void initialize(Project project);

  void dispose();

  default void fireStateChanged(Project project) {
    project.getMessageBus().syncPublisher(TOPIC).stateChanged(this);
  }

  interface Notifier {
    void stateChanged(AutoFetchAllowed allowed);
  }
}
