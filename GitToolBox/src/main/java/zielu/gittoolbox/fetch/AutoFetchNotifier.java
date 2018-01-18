package zielu.gittoolbox.fetch;

import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;

public interface AutoFetchNotifier {
  Topic<AutoFetchNotifier> TOPIC = Topic.create("Git ToolBox Auto Fetch", AutoFetchNotifier.class);

  void stateChanged(@NotNull AutoFetchState state);
}
