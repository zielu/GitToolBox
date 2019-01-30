package zielu.gittoolbox.fetch;

import com.google.common.base.Preconditions;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.config.ConfigNotifier;
import zielu.gittoolbox.config.GitToolBoxConfigForProject;

class AutoFetchSubscriber implements ProjectComponent {
  private final Project project;
  private AutoFetchComponent autoFetchComponent;
  private MessageBusConnection connection;

  AutoFetchSubscriber(@NotNull Project project) {
    this.project = project;
  }

  @Override
  public void initComponent() {
    autoFetchComponent = Preconditions.checkNotNull(project.getComponent(AutoFetchComponent.class));
    connectToMessageBus();
  }

  private void connectToMessageBus() {
    connection = project.getMessageBus().connect();
    connection.subscribe(ConfigNotifier.CONFIG_TOPIC, new ConfigNotifier.Adapter() {
      @Override
      public void configChanged(Project project, GitToolBoxConfigForProject previous,
                                GitToolBoxConfigForProject current) {
        autoFetchComponent.configChanged(current);
      }
    });
    connection.subscribe(AutoFetchNotifier.TOPIC, state -> autoFetchComponent.stateChanged(state));
  }

  @Override
  public void disposeComponent() {
    if (connection != null) {
      connection.disconnect();
      connection = null;
    }
    autoFetchComponent = null;
  }
}
