package zielu.gittoolbox.fetch;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import java.util.concurrent.atomic.AtomicBoolean;
import zielu.gittoolbox.extension.AutoFetchAllowed;

public class AutoFetchAllowedDumbMode implements AutoFetchAllowed {
  private final Logger log = Logger.getInstance(getClass());

  private final AtomicBoolean dumbMode = new AtomicBoolean();
  private MessageBusConnection connection;

  @Override
  public boolean isAllowed() {
    return !dumbMode.get();
  }

  @Override
  public void initialize(Project project) {
    connection = project.getMessageBus().connect();
    connection.subscribe(DumbService.DUMB_MODE, new DumbService.DumbModeListener() {
      @Override
      public void enteredDumbMode() {
        log.debug("Entered dumb mode");
        dumbMode.set(true);
      }

      @Override
      public void exitDumbMode() {
        log.debug("Exited dumb mode");
        dumbMode.set(false);
        fireStateChanged(project);
      }
    });
  }

  @Override
  public void dispose() {
    if (connection != null) {
      connection.disconnect();
      connection = null;
    }
  }
}
