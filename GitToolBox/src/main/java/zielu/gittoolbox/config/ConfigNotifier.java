package zielu.gittoolbox.config;

import com.intellij.openapi.project.Project;
import com.intellij.util.messages.Topic;

public interface ConfigNotifier {
  Topic<ConfigNotifier> CONFIG_TOPIC = Topic.create("Git ToolBox Config", ConfigNotifier.class);

  void configChanged(GitToolBoxConfig2 previous, GitToolBoxConfig2 current);

  void configChanged(Project project, GitToolBoxConfigForProject previous, GitToolBoxConfigForProject current);

  class Adapter implements ConfigNotifier {

    @Override
    public void configChanged(GitToolBoxConfig2 previous, GitToolBoxConfig2 current) {
    }

    @Override
    public void configChanged(Project project, GitToolBoxConfigForProject previous,
                              GitToolBoxConfigForProject current) {
    }
  }
}
