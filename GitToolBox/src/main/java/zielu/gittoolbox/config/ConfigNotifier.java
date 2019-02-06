package zielu.gittoolbox.config;

import com.intellij.openapi.project.Project;
import com.intellij.util.messages.Topic;

public interface ConfigNotifier {
  Topic<ConfigNotifier> CONFIG_TOPIC = Topic.create("Git ToolBox Config", ConfigNotifier.class);

  default void configChanged(GitToolBoxConfig2 previous, GitToolBoxConfig2 current) {
  }

  default void configChanged(Project project, GitToolBoxConfigForProject previous,
                             GitToolBoxConfigForProject current) {
  }
}
