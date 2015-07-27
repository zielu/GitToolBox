package zielu.gittoolbox;

import com.intellij.util.messages.Topic;

public interface GitToolBoxConfigNotifier {
    Topic<GitToolBoxConfigNotifier> CONFIG_TOPIC = Topic.create("Git ToolBox Config", GitToolBoxConfigNotifier.class);

    void configChanged(GitToolBoxConfig config);
}
