package zielu.gittoolbox.fetch;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.extension.AutoFetchAllowed;
import zielu.gittoolbox.extension.AutoFetchAllowedEP;

public class AutoFetchState implements ProjectComponent {
  private final Logger log = Logger.getInstance(getClass());

  private final AtomicBoolean fetchRunning = new AtomicBoolean();
  private final AtomicBoolean active = new AtomicBoolean();
  private final List<AutoFetchAllowed> extensions = new ArrayList<>();
  private final Project project;
  private MessageBusConnection connection;

  public AutoFetchState(@NotNull Project project) {
    this.project = project;
  }

  @SuppressFBWarnings({"NP_NULL_ON_SOME_PATH"})
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
    return Stream.of(Extensions.getExtensions(AutoFetchAllowedEP.POINT_NAME));
  }

  private AutoFetchAllowed instantiate(AutoFetchAllowedEP extensionPoint) {
    AutoFetchAllowed extension = extensionPoint.instantiate();
    extension.initialize(project);
    log.debug("Extension created: ", extension);
    return extension;
  }

  @Override
  public void projectOpened() {
    active.compareAndSet(false, true);
  }

  private void fireStateChanged() {
    project.getMessageBus().syncPublisher(AutoFetchNotifier.TOPIC).stateChanged(this);
  }

  @Override
  public void projectClosed() {
    active.compareAndSet(true, false);
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
    return active.get() && extensions.stream().allMatch(AutoFetchAllowed::isAllowed);
  }

  public boolean canAutoFetch() {
    return isFetchAllowed() && !fetchRunning.get();
  }

  public boolean fetchStart() {
    return fetchRunning.compareAndSet(false, true);
  }

  public void fetchFinish() {
    fetchRunning.compareAndSet(true, false);
  }
}
