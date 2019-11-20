package zielu.gittoolbox.fetch;

import com.intellij.openapi.components.BaseComponent;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.extension.AutoFetchAllowed;
import zielu.gittoolbox.extension.AutoFetchAllowedExtension;

public class AutoFetchState implements BaseComponent {
  private final AtomicBoolean fetchRunning = new AtomicBoolean();
  private final Project project;
  private final AutoFetchAllowedExtension extension;

  AutoFetchState(@NotNull Project project) {
    this.project = project;
    extension = new AutoFetchAllowedExtension(project);
  }

  @NotNull
  public static AutoFetchState getInstance(@NotNull Project project) {
    return project.getComponent(AutoFetchState.class);
  }

  @Override
  public void initComponent() {
    connectToMessageBus();
  }

  private void connectToMessageBus() {
    MessageBusConnection connection = project.getMessageBus().connect(project);
    connection.subscribe(AutoFetchAllowed.TOPIC, allowed -> fireStateChanged());
  }

  private void fireStateChanged() {
    project.getMessageBus().syncPublisher(AutoFetchNotifier.TOPIC).stateChanged(this);
  }

  boolean canAutoFetch() {
    return extension.isFetchAllowed() && !fetchRunning.get();
  }

  boolean fetchStart() {
    return fetchRunning.compareAndSet(false, true);
  }

  void fetchFinish() {
    fetchRunning.compareAndSet(true, false);
  }
}
