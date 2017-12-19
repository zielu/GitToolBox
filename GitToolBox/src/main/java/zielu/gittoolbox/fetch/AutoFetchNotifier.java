package zielu.gittoolbox.fetch;

import com.intellij.util.messages.Topic;

public interface AutoFetchNotifier {
  Topic<AutoFetchNotifier> TOPIC = Topic.create("Git ToolBox Auto Fetch", AutoFetchNotifier.class);

  void stateChanged(AutoFetchState state);
}
