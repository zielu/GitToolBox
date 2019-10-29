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
  private final AtomicBoolean extensionsLoaded = new AtomicBoolean();
  private final List<AutoFetchAllowed> extensions = new ArrayList<>();
  private final Project project;

  AutoFetchState(@NotNull Project project) {
    this.project = project;
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

  private Stream<AutoFetchAllowedEP> getExtensionPoints() {
    return AutoFetchAllowedEP.POINT_NAME.getExtensionList().stream();
  }

  private AutoFetchAllowed instantiate(AutoFetchAllowedEP extensionPoint) {
    AutoFetchAllowed extension = extensionPoint.instantiate(project);
    log.debug("Extension created: ", extension);
    extension.initialize();
    return extension;
  }

  private void fireStateChanged() {
    project.getMessageBus().syncPublisher(AutoFetchNotifier.TOPIC).stateChanged(this);
  }

  @Override
  public void disposeComponent() {
    disposeExtensions();
  }

  private void disposeExtensions() {
    extensions.clear();
  }

  private boolean isFetchAllowed() {
    if (extensionsLoaded.compareAndSet(false, true)) {
      extensions.addAll(getExtensionPoints().map(this::instantiate).collect(Collectors.toList()));
    }
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
