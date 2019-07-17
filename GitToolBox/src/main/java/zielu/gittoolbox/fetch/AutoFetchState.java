package zielu.gittoolbox.fetch;

import com.intellij.openapi.components.BaseComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.extension.AutoFetchAllowed;
import zielu.gittoolbox.extension.AutoFetchAllowedEP;

public class AutoFetchState implements BaseComponent {
  private final Logger log = Logger.getInstance(getClass());

  private final AtomicBoolean fetchRunning = new AtomicBoolean();
  private final List<AutoFetchAllowed> extensions = new ArrayList<>();
  private final Project project;
  private MessageBusConnection connection;

  AutoFetchState(@NotNull Project project) {
    this.project = project;
  }

  @NotNull
  public static AutoFetchState getInstance(@NotNull Project project) {
    return project.getComponent(AutoFetchState.class);
  }

  @Override
  public void initComponent() {
    initializeExtensions();
    connectToMessageBus();
  }

  private void initializeExtensions() {
    extensions.addAll(getExtensionPoints().map(this::instantiate).collect(Collectors.toList()));
  }

  private void connectToMessageBus() {
    connection = project.getMessageBus().connect();
    connection.subscribe(AutoFetchAllowed.TOPIC, allowed -> fireStateChanged());
  }

  private Stream<AutoFetchAllowedEP> getExtensionPoints() {
    return AutoFetchAllowedEP.POINT_NAME.getExtensionList().stream();
  }

  private AutoFetchAllowed instantiate(AutoFetchAllowedEP extensionPoint) {
    AutoFetchAllowed extension = extensionPoint.instantiate();
    extension.initialize(project);
    log.debug("Extension created: ", extension);
    return extension;
  }

  private void fireStateChanged() {
    project.getMessageBus().syncPublisher(AutoFetchNotifier.TOPIC).stateChanged(this);
  }

  @Override
  public void disposeComponent() {
    disconnectFromMessageBus();
    disposeExtensions();
  }

  private void disconnectFromMessageBus() {
    if (connection != null) {
      connection.disconnect();
      connection = null;
    }
  }

  private void disposeExtensions() {
    extensions.forEach(AutoFetchAllowed::dispose);
    extensions.clear();
  }

  private boolean isFetchAllowed() {
    return extensions.stream().allMatch(AutoFetchAllowed::isAllowed);
  }

  boolean canAutoFetch() {
    return isFetchAllowed() && !fetchRunning.get();
  }

  boolean fetchStart() {
    return fetchRunning.compareAndSet(false, true);
  }

  void fetchFinish() {
    fetchRunning.compareAndSet(true, false);
  }
}
