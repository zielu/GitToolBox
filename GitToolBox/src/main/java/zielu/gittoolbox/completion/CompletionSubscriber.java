package zielu.gittoolbox.completion;

import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.config.ConfigNotifier;
import zielu.gittoolbox.config.GitToolBoxConfigForProject;

class CompletionSubscriber {

  CompletionSubscriber(@NotNull Project project) {
    MessageBusConnection connection = project.getMessageBus().connect(project);
    connection.subscribe(ConfigNotifier.CONFIG_TOPIC, new ConfigNotifier() {
      @Override
      public void configChanged(Project project, GitToolBoxConfigForProject previous,
                                GitToolBoxConfigForProject current) {
        CompletionService.getExistingInstance(project).ifPresent(service -> service.onConfigChanged(current));
      }
    });
  }
}
