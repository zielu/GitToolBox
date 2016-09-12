package zielu.gittoolbox;

import com.intellij.openapi.project.Project;
import com.intellij.util.messages.Topic;

public interface ConfigNotifier {
    Topic<ConfigNotifier> CONFIG_TOPIC = Topic.create("Git ToolBox Config", ConfigNotifier.class);

    void configChanged(GitToolBoxConfig config);
    void configChanged(Project project, GitToolBoxConfigForProject config);

    class Adapter implements ConfigNotifier {

        @Override
        public void configChanged(GitToolBoxConfig config) {}

        @Override
        public void configChanged(Project project, GitToolBoxConfigForProject config) {}
    }
}
