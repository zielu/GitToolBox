package zielu.gittoolbox.extension;

import com.intellij.util.messages.Topic;

public interface AutoFetchAllowed {
  Topic<Notifier> TOPIC = Topic.create("Git ToolBox Auto Fetch Allowed", Notifier.class);

  void initialize();

  boolean isAllowed();

  interface Notifier {
    void stateChanged(AutoFetchAllowed allowed);
  }
}
