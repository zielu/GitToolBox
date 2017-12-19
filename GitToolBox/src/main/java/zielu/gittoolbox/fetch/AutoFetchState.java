package zielu.gittoolbox.fetch;

import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.Extensions;
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

public class AutoFetchState extends AbstractProjectComponent {
  private final Logger log = Logger.getInstance(getClass());

  private final AtomicBoolean fetchRunning = new AtomicBoolean();
  private final AtomicBoolean active = new AtomicBoolean();
  private final List<AutoFetchAllowed> extensions = new ArrayList<>();
  private MessageBusConnection connection;

  public AutoFetchState(Project project) {
    super(project);
  }

  public static AutoFetchState getInstance(@NotNull Project project) {
    return project.getComponent(AutoFetchState.class);
  }

  @Override
  public void initComponent() {
    extensions.addAll(getExtensionPoints().map(this::instantiate).collect(Collectors.toList()));
    connection = myProject.getMessageBus().connect();
    connection.subscribe(AutoFetchAllowed.TOPIC, allowed -> fireStateChanged());
  }

  private Stream<AutoFetchAllowedEP> getExtensionPoints() {
    return Stream.of(Extensions.getExtensions(AutoFetchAllowedEP.POINT_NAME));
  }

  private AutoFetchAllowed instantiate(AutoFetchAllowedEP extensionPoint) {
    AutoFetchAllowed extension = extensionPoint.instantiate();
    extension.initialize(myProject);
    log.debug("Extension created: ", extension);
    return extension;
  }

  @Override
  public void projectOpened() {
    active.compareAndSet(false, true);
  }

  private void fireStateChanged() {
    myProject.getMessageBus().syncPublisher(AutoFetchNotifier.TOPIC).stateChanged(this);
  }

  @Override
  public void projectClosed() {
    active.compareAndSet(true, false);
  }

  @Override
  public void disposeComponent() {
    if (connection != null) {
      connection.disconnect();
      connection = null;
    }
    List<AutoFetchAllowed> allowed = new ArrayList<>(extensions);
    extensions.clear();
    allowed.forEach(AutoFetchAllowed::dispose);
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
